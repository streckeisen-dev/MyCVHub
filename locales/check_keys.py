import yaml
import os
import sys

# Load YAML file and flatten it into key-value pairs
def load_and_flatten_yaml(yaml_file):
    with open(yaml_file, 'r', encoding='utf-8') as file:
        data = yaml.safe_load(file)
    return flatten_dict(data)

# Flatten nested dictionary into a single level with dot notation keys
def flatten_dict(d, parent_key='', sep='.'):
    items = []
    for key, value in d.items():
        new_key = f"{parent_key}{sep}{key}" if parent_key else key
        if isinstance(value, dict):
            items.extend(flatten_dict(value, new_key, sep=sep).items())
        else:
            items.append((new_key, value))
    return dict(items)

# Compare all keys across all YAML files
def compare_all_keys(yaml_files):
    all_keys = set()  # Combined set of keys across all files
    file_key_map = {}  # Store each file's flattened key set

    # Load and flatten all files
    for yaml_file in yaml_files:
        flattened_keys = load_and_flatten_yaml(yaml_file)
        file_key_map[yaml_file] = set(flattened_keys.keys())
        all_keys.update(flattened_keys.keys())

    # Check for missing keys in each file
    missing_keys = {}
    for yaml_file, keys in file_key_map.items():
        missing_in_file = all_keys - keys  # Keys that are in the total set but missing in the current file
        if missing_in_file:
            missing_keys[yaml_file] = missing_in_file

    return missing_keys

# Print out the results and return an exit code
def report_missing_keys(missing_keys):
    if not missing_keys:
        print("All keys are present in all files.")
        return 0
    else:
        for yaml_file, keys in missing_keys.items():
            print(f"Missing keys in {yaml_file}:")
            for key in keys:
                print(f"  - {key}")
        return 1  # Non-zero exit code indicates failure

# Main function
def check_yaml_files(yaml_directory):
    # List all .yml files in the directory
    yaml_files = [os.path.join(yaml_directory, file) for file in os.listdir(yaml_directory) if file.endswith('.yml')]

    if len(yaml_files) < 2:
        print("At least two YAML files are required for comparison.")
        return 1

    # Compare keys across all files
    missing_keys = compare_all_keys(yaml_files)

    # Report missing keys and return an exit code
    return report_missing_keys(missing_keys)

yaml_directory = './'  # Directory where your .yml files are stored
exit_code = check_yaml_files(yaml_directory)
sys.exit(exit_code)