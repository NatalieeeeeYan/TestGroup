import hashlib
import pickle
from typing import Dict, Sequence, Any

from schedule.PowerSchedule import PowerSchedule
from utils.Seed import Seed
import hashlib
import pickle

def get_path_id(coverage: Any) -> str:
    """返回路径的哈希值"""
    pickled = pickle.dumps(sorted(coverage))
    return hashlib.md5(pickled).hexdigest()

# 根据 inputs 经过的路径频率动态选择 Seed
class MyPathPowerSchedule(PowerSchedule):

    def __init__(self, exp) -> None:
        super().__init__()
        # TODO
        self.exp = exp # 指数级
        self.path_frequency: Dict = {}
        self.novelty_scores = {}
        self.recent_discoveries: Dict = {}

    def assign_energy(self, population: Sequence[Seed]) -> None:
        """Assign exponential energy inversely proportional to path frequency"""
        # TODO
        # for seed in population:
        #     freq = self.path_frequency[get_path_id(seed.coverage)]
        #     seed.energy = 1 / (freq ** self.exp)
        # print(self.recent_discoveries)
        total_discoveries = sum(self.recent_discoveries.values())
        for seed in population:
            path_id = get_path_id(seed.coverage)
            freq = self.path_frequency[path_id]
            discovery_score = self.recent_discoveries.get(path_id, 0) / (total_discoveries + 1)
            # print(discovery_score)
            seed.energy = (1 / (freq ** self.exp)) * (1 + discovery_score)