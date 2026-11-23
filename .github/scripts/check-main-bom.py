"""
Checks that every module of this repository that users depend on is listed in langchain4j-bom
(langchain4j-bom/pom.xml in https://github.com/langchain4j/langchain4j). That BOM is the only one
that manages the versions of these modules, so a module missing from it has to be versioned by hand.

Usage, from the root of this repository:
    python3 .github/scripts/check-main-bom.py [--warn-only] [<path or URL of langchain4j-bom/pom.xml>]
By default, the BOM is read from the main branch of langchain4j.
With --warn-only, missing modules are reported as GitHub Actions warnings and the check does not fail
(used on pull requests: a contributor cannot add a new module to langchain4j-bom in the same PR).
"""

import sys
import urllib.request
import xml.etree.ElementTree as ET

MAIN_BOM_URL = "https://raw.githubusercontent.com/langchain4j/langchain4j/main/langchain4j-bom/pom.xml"
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def module_artifact_ids():
    artifact_ids = []
    for module in ET.parse("pom.xml").getroot().findall("m:modules/m:module", NS):
        artifact_id = ET.parse(f"{module.text}/pom.xml").getroot().findtext("m:artifactId", namespaces=NS)
        # *-tests modules (shared test code and the integration test applications) are not for users
        if not artifact_id.endswith("-tests"):
            artifact_ids.append(artifact_id)
    return artifact_ids


def bom_artifact_ids(location):
    if location.startswith("https://"):
        with urllib.request.urlopen(location, timeout=60) as response:
            root = ET.fromstring(response.read())
    else:
        root = ET.parse(location).getroot()
    return {a.text for a in root.findall("m:dependencyManagement/m:dependencies/m:dependency/m:artifactId", NS)}


args = sys.argv[1:]
warn_only = "--warn-only" in args
locations = [arg for arg in args if arg != "--warn-only"]
bom_location = locations[0] if locations else MAIN_BOM_URL
modules = module_artifact_ids()
in_bom = bom_artifact_ids(bom_location)
missing = [artifact_id for artifact_id in modules if artifact_id not in in_bom]

if missing:
    how_to_fix = ("Add them to langchain4j-bom/pom.xml in https://github.com/langchain4j/langchain4j "
                  "(with <version>${langchain4j.beta.version}</version>, like the other Spring modules there).")
    if warn_only:
        print(f"::warning title=Missing from langchain4j-bom::{', '.join(missing)}. {how_to_fix}")
        sys.exit(0)
    print(f"These modules are missing from langchain4j-bom ({bom_location}):")
    for artifact_id in missing:
        print(f"  - {artifact_id}")
    print(how_to_fix)
    sys.exit(1)

print(f"All {len(modules)} modules are listed in langchain4j-bom ({bom_location}).")
