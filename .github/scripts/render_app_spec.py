#!/usr/bin/env python3
import os
import re
import sys
import json
from pathlib import Path

PLACEHOLDER_PATTERN = re.compile(r"\$\{\{\s*(secrets|env)\.([A-Za-z_]\w*)\s*}}")
GITHUB_SHA_PATTERN = re.compile(r"\$\{\{\s*github\.sha\s*}}")
REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
EXPECTED_SOURCE = REPOSITORY_ROOT / ".do" / "my-cv-app.yaml"
EXPECTED_TARGET = REPOSITORY_ROOT / ".do" / "my-cv-app.generated.yaml"


def render_app_spec(source: Path, target: Path) -> None:
    content = source.read_text()
    missing = set()

    def replace_secret_or_env(match: re.Match[str]) -> str:
        variable_name = match.group(2)
        value = os.environ.get(variable_name)
        if value is None or value == "":
            missing.add(variable_name)
            return match.group(0)
        return quote_yaml_scalar(value)

    rendered = PLACEHOLDER_PATTERN.sub(replace_secret_or_env, content)
    github_sha = os.environ.get("GITHUB_SHA")
    if github_sha is None or github_sha == "":
        missing.add("GITHUB_SHA")
    else:
        rendered = GITHUB_SHA_PATTERN.sub(quote_yaml_scalar(github_sha), rendered)

    fail_if_unresolved_placeholders(rendered, missing)

    target.write_text(rendered)


def quote_yaml_scalar(value: str) -> str:
    return json.dumps(value)


def fail_if_unresolved_placeholders(rendered: str, missing: set[str]) -> None:
    unresolved = sorted(set(re.findall(r"\$\{\{[^}]+}}", rendered)))
    if not missing and not unresolved:
        return
    print_missing_values(missing)
    print_unresolved_placeholders(unresolved)
    sys.exit(1)


def print_missing_values(missing: set[str]) -> None:
    if not missing:
        return
    print("Missing environment values for app spec placeholders:", file=sys.stderr)
    for variable_name in sorted(missing):
        print(f"- {variable_name}", file=sys.stderr)


def print_unresolved_placeholders(unresolved: list[str]) -> None:
    if not unresolved:
        return
    print("Unresolved app spec placeholders:", file=sys.stderr)
    for placeholder in unresolved:
        print(f"- {placeholder}", file=sys.stderr)


def require_expected_path(path: Path, expected_path: Path, label: str) -> Path:
    resolved_path = (REPOSITORY_ROOT / path).resolve() if not path.is_absolute() else path.resolve()
    if resolved_path != expected_path:
        print(f"Invalid {label} path: expected {expected_path}", file=sys.stderr)
        sys.exit(2)
    return resolved_path


def main() -> None:
    if len(sys.argv) != 3:
        print("Usage: render_app_spec.py <source-spec> <target-spec>", file=sys.stderr)
        sys.exit(2)

    source = require_expected_path(Path(sys.argv[1]), EXPECTED_SOURCE, "source")
    target = require_expected_path(Path(sys.argv[2]), EXPECTED_TARGET, "target")
    render_app_spec(source, target)


if __name__ == "__main__":
    main()
