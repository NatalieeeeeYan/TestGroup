from typing import Dict, List, Sequence

from schedule.PowerSchedule import PowerSchedule
from utils.Seed import Seed
import hashlib
import pickle

class CoveragePowerSchedule(PowerSchedule):
    
    def __init__(self) -> None:
        super().__init__()
        
    def assign_energy(self, population: List[Seed]) -> None:
        """Assign higher energy to seeds that cover more lines of code"""
        for seed in population:
            seed.energy = len(seed.coverage)