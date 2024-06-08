from typing import Dict, List, Sequence

from schedule.PowerSchedule import PowerSchedule
from utils.Seed import Seed
from typing import List, Any, Tuple, Set
from utils.Coverage import Coverage, Location
import hashlib
import pickle

def get_path_id(coverage: Any) -> str:
    """返回路径的哈希值"""
    pickled = pickle.dumps(sorted(coverage))
    return hashlib.md5(pickled).hexdigest()

class CoveragePowerSchedule(PowerSchedule):
    
    def __init__(self) -> None:
        super().__init__()
        self.path_frequency: Dict = {}
        self.novelty_scores: Dict = {}
        
    def assign_energy(self, population: List[Seed]) -> None:
        """Assign higher energy to seeds that cover more lines of code"""
        # for seed in population:
        #     seed.energy = len(seed.coverage)
        for seed in population:
            # FIXME: load seed coverage
            novelty_score = self.novelty_scores.get(get_path_id(seed.load_coverage()), 0) 
            if novelty_score == 0:
                freq = self.path_frequency[get_path_id(seed.load_coverage())]
                novelty_score = 1 / freq
            seed.energy = novelty_score
            # print(seed.energy)
