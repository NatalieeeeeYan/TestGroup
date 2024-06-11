from utils.Mutator import *
from utils.ObjectUtils import dump_object, load_object, get_md5_of_object
# s = "hello, world!"
# print("Original string:", s)
# print("Insert random character:", insert_random_character(s))
# print("Flip random bits:", flip_random_bits(s))
# print("Arithmetic random bytes:", arithmetic_random_bytes(s))
# print("Interesting random bytes:", interesting_random_bytes(s))
# print("Random insert:", havoc_random_insert(s))
# print("Random replace:", havoc_random_replace(s))
# print("Delete random bytes:", delete_random_bytes(s))
# print("Change case:", change_case(s))

# arithmetic_random_bytes,
# interesting_random_bytes,
# havoc_random_insert,
# havoc_random_replace,
# delete_random_bytes,
# change_case,
            # insert_random_html_tag,
            # delete_random_html_tag,
            # replace_random_html_tag,
            # insert_random_html_attribute,
            # insert_random_html_entity,
            # insert_random_html_comment
init_seed_data = load_object("corpus/corpus_4")
print(init_seed_data[1])
print(len(init_seed_data[1]))