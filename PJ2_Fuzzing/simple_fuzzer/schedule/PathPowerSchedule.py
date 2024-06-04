from typing import Dict, Sequence

from schedule.PowerSchedule import PowerSchedule
from utils.Seed import Seed
import hashlib
import pickle

def get_path_id(coverage: Any) -> str:
    """返回路径的哈希值"""
    pickled = pickle.dumps(sorted(coverage))
    return hashlib.md5(pickled).hexdigest()


class PathPowerSchedule(PowerSchedule):

    def __init__(self, exponent: float) -> None:
        super().__init__()
        # TODO
        self.exponent = exponent  # 控制能量分配的指数值
        self.path_frequency: Dict[str, int] = {}  # 用于记录每条路径的频率
        
    def assign_energy(self, population: Sequence[Seed]) -> None:
        """Assign exponential energy inversely proportional to path frequency"""
        # TODO

        for seed in population:
            # 获取种子的路径
            path = get_path_id(seed.coverage)
            # 记录路径的频率
            self.path_frequency[path] = self.path_frequency.get(path, 0) + 1
            
        # 计算路径的能量
        for seed in population:
            path = get_path_id(seed.coverage)
            frequency = self.path_frequency[path]
            if frequency == 0:
                seed.energy = 1.0  # 避免除以零
            else:
                seed.energy = 1 / (frequency ** self.exponent)  # 路径频率越高，能量越低
                
            