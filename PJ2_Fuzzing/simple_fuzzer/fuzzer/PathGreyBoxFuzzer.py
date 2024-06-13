import time
from typing import List, Tuple, Any

from fuzzer.GreyBoxFuzzer import GreyBoxFuzzer
from schedule.PathPowerSchedule import PathPowerSchedule, get_path_id
from runner.FunctionCoverageRunner import FunctionCoverageRunner


class PathGreyBoxFuzzer(GreyBoxFuzzer):
    """Count how often individual paths are exercised."""

    def __init__(self, seeds: List[str], schedule: PathPowerSchedule, is_print: bool):
        super().__init__(seeds, schedule, False)

        # TODO

        print("""
┌───────────────────────┬───────────────────────┬───────────────────────┬───────────────────┬───────────────────┬────────────────┬───────────────────┐
│        Run Time       │     Last New Path     │    Last Uniq Crash    │    Total Execs    │    Total Paths    │  Uniq Crashes  │   Covered Lines   │
├───────────────────────┼───────────────────────┼───────────────────────┼───────────────────┼───────────────────┼────────────────┼───────────────────┤""")

    def print_stats(self):
        def format_seconds(seconds):
            hours = int(seconds) // 3600
            minutes = int(seconds % 3600) // 60
            remaining_seconds = int(seconds) % 60
            return f"{hours:02d}:{minutes:02d}:{remaining_seconds:02d}"

        template = """│{runtime}│{path_time}│{crash_time}│{total_exec}│{total_path}│{uniq_crash}│{covered_line}│
├───────────────────────┼───────────────────────┼───────────────────────┼───────────────────┼───────────────────┼────────────────┼───────────────────┤"""
        template = template.format(runtime=format_seconds(time.time() - self.start_time).center(23),
                                   path_time="".center(23),
                                   crash_time=format_seconds(self.last_crash_time - self.start_time).center(23),
                                   total_exec=str(self.total_execs).center(19),
                                   total_path="".center(19),
                                   uniq_crash=str(len(set(self.crash_map.values()))).center(16),
                                   covered_line=str(len(self.covered_line)).center(19))
        print(template)

    def run(self, runner: FunctionCoverageRunner) -> Tuple[Any, str]:  # type: ignore
        """Inform scheduler about path frequency"""
       
        result, outcome = super().run(runner)
        # TODO
        path_id = get_path_id(runner.coverage())
        if path_id not in self.schedule.path_frequency:
            self.schedule.path_frequency[path_id] = 1
            self.last_path_time = time.time()
        else:
            self.schedule.path_frequency[path_id] += 1

        if self.new_coverage:
            new_path_id = get_path_id(self.new_coverage)
            if new_path_id not in self.schedule.novelty_scores:
                self.schedule.novelty_scores[new_path_id] = 1
                self.last_path_time = time.time()
            else:
                self.schedule.novelty_scores[new_path_id] += 1

        # if self.recent_discoveries:
            # path_id = get_path_id(self.s.coverage)
        if path_id not in self.schedule.recent_discoveries:
            self.schedule.recent_discoveries[path_id] = 0
        self.schedule.recent_discoveries[path_id] += 1

        return result, outcome
