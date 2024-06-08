import os
import time

from fuzzer.PathGreyBoxFuzzer import PathGreyBoxFuzzer
from runner.FunctionCoverageRunner import FunctionCoverageRunner
from schedule.PathPowerSchedule import PathPowerSchedule
from schedule.CoveragePowerSchedule import CoveragePowerSchedule
from samples.Samples import sample1, sample2, sample3, sample4
from utils.ObjectUtils import dump_object, load_object, get_md5_of_object
from utils.Seed import Seed

SEED_DIRECTORY = 'seeds'


class Result:
    def __init__(self, coverage, crashes, start_time, end_time):
        self.covered_line = coverage
        self.crashes = crashes
        self.start_time = start_time
        self.end_time = end_time

    def __str__(self):
        return "Covered Lines: " + str(self.covered_line) +  "Covered Lines len: " + str(len(self.covered_line)) +" , Crashes Num: " + str(self.crashes) + ", Start Time: " + str(self.start_time) + ", End Time: " + str(self.end_time)


if __name__ == "__main__":

    f_runner = FunctionCoverageRunner(sample4)
    
    # change seed path here: 
    init_path = "corpus/corpus_4"
    init_seed_data = load_object("corpus/corpus_4")
    seeds_folder = os.path.join(SEED_DIRECTORY, init_path.split('/')[-1])
    if not os.path.exists(seeds_folder):
        os.makedirs(seeds_folder)
    
    seeds = []
    for init_data in init_seed_data:
        # seeds.append(Seed(data=init_data, _coverage=set(), path="corpus/corpus_4", directory=SEED_DIRECTORY))
        hash_value = get_md5_of_object(obj=init_data)
        path = os.path.join(seeds_folder, hash_value + ".seed")
        dump_object(path=path, data=init_data)
        seeds.append(Seed(data=init_data, _coverage=set(), path=path, directory=SEED_DIRECTORY))

    # grey_fuzzer = PathGreyBoxFuzzer(seeds=seeds, schedule=PathPowerSchedule(8), is_print=True)
    grey_fuzzer = PathGreyBoxFuzzer(seeds=seeds, schedule=CoveragePowerSchedule(), is_print=True)
    start_time = time.time()
    grey_fuzzer.runs(f_runner, run_time=600)
    res = Result(grey_fuzzer.covered_line, set(grey_fuzzer.crash_map.values()), start_time, time.time())
    dump_object("_result" + os.sep + "Sample-4.pkl", res)
    print(load_object("_result" + os.sep + "Sample-4.pkl"))
    end_time = time.time()
    print(f'Finished with time: {end_time - start_time}')
