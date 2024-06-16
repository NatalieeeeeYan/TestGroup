import os
import time
from typing import List, Any, Tuple, Set

import random

from fuzzer.Fuzzer import Fuzzer
from runner.Runner import Runner
from utils.Coverage import Location
from utils.Mutator import Mutator
from runner.FunctionCoverageRunner import FunctionCoverageRunner
from schedule.PowerSchedule import PowerSchedule

from utils.Seed import Seed, save_seed
from utils.ObjectUtils import get_md5_of_object

import logging

log_file = 'seeds_persistance.log'
log_dir = 'logs'

if not os.path.exists(log_dir):
    os.makedirs(log_dir)

log_path = os.path.join(log_dir, log_file)
log_format = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
log_level = logging.INFO
logging.basicConfig(filename=log_path, filemode='a', format=log_format, level=log_level)
logger = logging.getLogger(__name__)


class GreyBoxFuzzer(Fuzzer):

    def __init__(self, seeds: List[str], schedule: PowerSchedule, is_print: bool, seed_directory: str = './seeds') -> None:
        """Constructor.
        `seeds` - a list of (input) strings to mutate.
        `mutator` - the mutator to apply.
        `schedule` - the power schedule to apply.
        """
        super().__init__()
        self.last_crash_time = self.start_time
        self.population = []
        self.file_map = {}
        self.covered_line: Set[Location] = set()
        self.seed_index = 0
        self.crash_map = dict()
        self.seeds = seeds
        self.mutator = Mutator()
        self.schedule = schedule
        self.new_coverage = set()
        self.seed_directory = seed_directory
        if is_print:
            print("""
┌───────────────────────┬───────────────────────┬───────────────────┬────────────────┬───────────────────┐
│        Run Time       │    Last Uniq Crash    │    Total Execs    │  Uniq Crashes  │   Covered Lines   │
├───────────────────────┼───────────────────────┼───────────────────┼────────────────┼───────────────────┤""")


    def create_candidate(self) -> str:
        """Returns an input generated by fuzzing a seed in the population"""
        seed = self.schedule.choose(self.population)
        
        # Choose seed from population -> choose path and load seeds [lazy load]
        data = seed.load_data()
        logger.info(f"Creating candidate from seed: {seed.id}")

        # Stacking: Apply multiple mutations to generate the candidate
        candidate = data
        trials = min(len(candidate), 1 << random.randint(1, 5))
        for i in range(trials):
            candidate = self.mutator.mutate(candidate)
        return candidate

    def fuzz(self) -> str:
        """Returns first each seed once and then generates new inputs"""
        if self.seed_index < len(self.seeds):
            # Still seeding
            self.inp = self.seeds[self.seed_index].load_data()
            self.seed_index += 1
        else:
            # Mutating
            self.inp = self.create_candidate()

        return self.inp

    def print_stats(self):
        def format_seconds(seconds):
            hours = int(seconds) // 3600
            minutes = int(seconds % 3600) // 60
            remaining_seconds = int(seconds) % 60
            return f"{hours:02d}:{minutes:02d}:{remaining_seconds:02d}"

        template = """│{runtime}│{crash_time}│{total_exec}│{uniq_crash}│{covered_line}│
├───────────────────────┼───────────────────────┼───────────────────┼────────────────┼───────────────────┤"""

        template = template.format(runtime=format_seconds(time.time() - self.start_time).center(23),
                                   crash_time=format_seconds(self.last_crash_time - self.start_time).center(23),
                                   total_exec=str(self.total_execs).center(19),
                                   uniq_crash=str(len(set(self.crash_map.values()))).center(16),
                                   covered_line=str(len(self.covered_line)).center(19))
        print(template)

    def run(self, runner: FunctionCoverageRunner) -> Tuple[Any, str]:  # type: ignore
        """Run function(inp) while tracking coverage.
           If we reach new coverage,
           add inp to population and its coverage to population_coverage
        """
        result, outcome = super().run(runner)
        if len(self.covered_line) != len(runner.all_coverage):
            self.new_coverage = runner.all_coverage - self.covered_line
            # print(self.new_coverage)
            self.covered_line |= runner.all_coverage
            if outcome == Runner.PASS:
                # We have new coverage
                # print("new")
                seed = Seed(data=self.inp, _coverage=runner.coverage(), path=None, directory=self.seed_directory)
                self.population.append(seed)        
                # Mutated seeds are appened to population -> gen seed and save, add hash/path value into population
                logger.info(f"New seed added with coverage: {seed.id}")
        if outcome == Runner.FAIL:
            uniq_crash_num = len(set(self.crash_map.values()))
            self.crash_map[self.inp] = result
            if len(set(self.crash_map.values())) != uniq_crash_num:
                self.last_crash_time = time.time()
            input_hash_id = get_md5_of_object(obj=self.inp)
            save_seed(data=self.inp, coverage=runner.coverage(), path=str(input_hash_id), seed_dir=self.seed_directory)
        
        return result, outcome
