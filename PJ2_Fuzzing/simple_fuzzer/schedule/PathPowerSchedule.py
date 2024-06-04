import hashlib
import pickle
from typing import Dict, Sequence, Any

from schedule.PowerSchedule import PowerSchedule
from utils.Seed import Seed

def get_path_id(coverage: Any) -> str:
    """Returns a unique hash for the covered statements"""
    pickled = pickle.dumps(sorted(coverage))
    return hashlib.md5(pickled).hexdigest()

# 根据 inputs 经过的路径频率动态选择 Seed
class PathPowerSchedule(PowerSchedule):

    def __init__(self, exp) -> None:
        super().__init__()
        # TODO
        self.exp = exp # 指数级
        self.path_frequency: Dict = {}

    def assign_energy(self, population: Sequence[Seed]) -> None:
        """Assign exponential energy inversely proportional to path frequency"""
        # TODO
        for seed in population:
            freq = self.path_frequency[get_path_id(seed.coverage)]
            seed.energy = 1 / (freq ** self.exp)