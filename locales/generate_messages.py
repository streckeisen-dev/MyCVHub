import yaml
import json
import os
import re
import glob
import argparse

# Load the YAML file
def load_yaml(yaml_file):
    with open(yaml_file, 'r', encoding='utf-8') as file:
        return yaml.safe_load(file)

# Convert nested dictionary to flattened key-value pairs
def flatten_dict(d, parent_key='', sep='.'):
    items = []
    for key, value in d.items():
        new_key = f"{parent_key}{sep}{key}" if parent_key else key
        if isinstance(value, dict):
            items.extend(flatten_dict(value, new_key, sep=sep).items())
        else:
            items.append((new_key, value))
    return dict(items)

# Merge keys from shared and specific sections (frontend/backend)
def merge_keys(shared, specific):
    combined = shared.copy()  # Start with shared keys

    for key, value in specific.items():
        if key in combined:
            # If the key already exists and both are dictionaries, merge them
            if isinstance(combined[key], dict) and isinstance(value, dict):
                combined[key] = merge_keys(combined[key], value)
            else:
                pass  # Keeping the existing value
        else:
            combined[key] = value
    return combined

# Write frontend JSON format
def write_frontend_json(locales, output_dir):
    for lang, content in locales.items():
        # Merge shared and frontend sections
        merged_data = merge_keys(content.get("shared", {}), content.get("frontend", {}))

        # Define the output path for each language
        output_file = os.path.join(output_dir, f"{lang}.json")

        # Write the merged data to the JSON file
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(merged_data, f, ensure_ascii=False, indent=2)

# Convert named parameters to indexed parameters for backend
def convert_named_to_indexed(message):
    named_params = re.findall(r'\{(\w+)\}', message)
    for i, param in enumerate(named_params):
        message = message.replace(f'{{{param}}}', f'{{{i}}}')
    return message

# Write backend properties format with prefix
def write_backend_properties(locales, output_dir, key_prefix, fallback_locale):
    for lang, content in locales.items():
        filename = f"messages_{lang}.properties" if lang != fallback_locale else "messages.properties"
        filepath = os.path.join(output_dir, filename)

        # Flatten shared and backend dictionaries for the properties file
        shared_data = flatten_dict(content.get("shared", {}), sep='.')
        backend_data = flatten_dict(content.get("backend", {}), sep='.')

        # Combine shared and backend for the properties file
        backend_combined = {**shared_data, **backend_data}

        with open(filepath, 'w', encoding='utf-8') as f:
            for key, value in backend_combined.items():
                prefixed_key = f"{key_prefix}.{key}"  # Add the prefix to the key
                escaped_value = value.replace("'", "''")
                f.write(f"{prefixed_key}={escaped_value}\n")

# Main function
def generate_i18n_files(gen_type, frontend_output_dir, backend_output_dir, key_prefix, fallback_locale):
    locales = {}

    # Find and load all YAML files
    yaml_files = glob.glob(os.path.join('./', 'messages_*.yml'))
    
    for yaml_file in yaml_files:
        data = load_yaml(yaml_file)

        # Extract language code from file name
        lang = os.path.basename(yaml_file).split('_')[1].split('.')[0]  # Example: messages_en.yml -> en
        locales[lang] = data

    # Ensure required sections are present
    if not locales:
        raise KeyError("No valid locales found in the YAML files.")

    # Write frontend (JSON) and backend (.properties) files
    if gen_type in ("all", "frontend"):
        write_frontend_json(locales, frontend_output_dir)
        print("Generated frontend locales")
    if gen_type in ("all", "backend"):
        write_backend_properties(locales, backend_output_dir, key_prefix, fallback_locale)
        print("Generated backend locales")

parser = argparse.ArgumentParser(description="Generate i18n files for frontend and backend.")
parser.add_argument(
    "--type", 
    choices=["all", "frontend", "backend"], 
    default="all",
    help="Specify which files to generate: 'all', 'frontend', or 'backend'."
)
args = parser.parse_args()

try:
    generate_type = args.type  # Command-line parameter for generating files
    frontend_output_dir = '../frontend/src/locales/'  # Path to output JSON for frontend
    backend_output_dir = '../backend/src/main/resources/'  # Relative output directory for backend .properties files
    key_prefix = 'ch.streckeisen.mycv'  # Prefix for backend keys
    fallback_locale = 'en'

    generate_i18n_files(generate_type, frontend_output_dir, backend_output_dir, key_prefix, fallback_locale)
    print("Files generated successfully!")
except Exception as e:
    print(f"An unexpected error occurred: {e}")