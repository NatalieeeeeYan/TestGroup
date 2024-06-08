from typing import Set, Union
import os

from utils.Coverage import Location
from utils.ObjectUtils import dump_object, load_object, get_md5_of_object
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


class Seed:
    """Represent an input with additional attributes"""

    def __init__(self, data: str, _coverage: Set[Location], path: str = None, directory: str = './seeds') -> None:
        """Initialize from seed data"""
        # self.data = data
        # These will be needed for advanced power schedules
        # self.coverage: Set[Location] = _coverage
        self.energy = 0.0

        if data is not None:
            self.id = get_md5_of_object(data)
            self.path = path if path else os.path.join(directory, f"{self.id}.seed")
            self.save(data, _coverage)
        else:
            self.id = None
            self.path = path

    def __str__(self) -> str:
        """Returns data as string representation of the seed"""
        # return self.data
        data = self.load_data()
        return data if data else ''
    
    __repr__ = __str__

    def save(self, data: str, coverage: Set[Location], directory: str = './seeds') -> None:
        """Save the seed to disk"""
        # if not os.path.exists(directory):
        #     os.makedirs(directory)
        # self.path = os.path.join(directory, self.path.split('/')[-1])
        dump_object(self.path, {
            'data': data,
            'coverage': coverage
        })
        logger.info(f"Seed saved to {self.path}")
    
    def load_data(self) -> str:
        """Load the seed from disk"""
        if not os.path.exists(self.path):
            logger.warning("Seed path is not set. Nothing to load.")
            raise(FileNotFoundError(f"Seed file not found: {self.path}"))
            return None
        seed = load_object(self.path)
        data = seed['data']
        # coverage = seed['coverage']
        logger.info(f"Seed data loaded from {self.path}")
        return data

    def load_coverage(self) -> str:
        """Load the seed coverage from disk"""
        if not os.path.exists(self.path):
            logger.warning("Seed path is not set. Nothing to load.")
            raise(FileNotFoundError(f"Seed file not found: {self.path}"))
            return None
        seed = load_object(self.path)
        # data = seed['data']
        coverage = seed['coverage']
        logger.info(f"Seed coverage loaded from {self.path}")
        return coverage
    