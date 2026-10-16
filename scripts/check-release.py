#!/usr/bin/env python3
"""Validate a GitHub Release tag against the committed plugin version."""

import os
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
NUMERIC = r"(?:0|[1-9][0-9]*)"
IDENTIFIER = rf"(?:{NUMERIC}|[0-9]*[A-Za-z-][0-9A-Za-z-]*)"
SEMVER = rf"{NUMERIC}\.{NUMERIC}\.{NUMERIC}(?:-{IDENTIFIER}(?:\.{IDENTIFIER})*)?(?:\+[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?"


def check_release(tag: str, properties: str, manifest: str) -> str:
    version = tag.removeprefix("v")
    if not re.fullmatch(SEMVER, version):
        raise ValueError("Release tag must be a SemVer such as v2.0.0 or v2.1.0-rc.1")
    gradle_version = re.search(r"(?m)^version=([^\s]+)$", properties)
    plugin_version = re.search(r'(?m)^  version: [\"\']?([^\s\"\']+)[\"\']?$', manifest)
    if not gradle_version or not plugin_version:
        raise ValueError("Version is missing from gradle.properties or plugin.yaml")
    if {version, gradle_version.group(1), plugin_version.group(1)} != {version}:
        raise ValueError("Release tag, gradle.properties and plugin.yaml must use the same version")
    if not re.search(r"(?m)^  name: han-equipment$", manifest):
        raise ValueError("Plugin metadata.name must be han-equipment")
    return version


if __name__ == "__main__":
    try:
        version = check_release(
            os.environ.get("RELEASE_TAG", ""),
            (ROOT / "gradle.properties").read_text(),
            (ROOT / "src/main/resources/plugin.yaml").read_text(),
        )
    except ValueError as error:
        print(f"Release validation failed: {error}", file=sys.stderr)
        sys.exit(1)
    print(f"Release version verified: {version}")
