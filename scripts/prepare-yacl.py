"""Build the pinned upstream YACL Reforged source into a project-local Maven repo.

Usage: python scripts/prepare-yacl.py [--java-home /path/to/jdk25]
Requires Python 3.11+, JDK 25 and access to GitHub/Gradle/Maven repositories.
No upstream source files are changed. The dependency remains a separate mod.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[1]
PINS = {
    "26.1": ("3.9.7+26.1-26.1.2", "90dde5edfe78f27649f9bd27463cd99408e755a1d942f6913eb666995ec27b82", "ab2bc9ba56f31c5e0c4fa4a9a0c578e741268b22"),
    "26.2": ("3.9.7+26.2", "de0c414643fd9688c17a91ce7390663b04c4a4fedbe41f43c8a65cfebf9ffe2a", "0d92aeb8865ca08fa401313bc0a918cdf871f0b2"),
    "26.3": ("3.9.7+26.3", "ed08e57928eae7921b9b23dad0adbc8c17ee1492b1f01000b3776ea4f3c1a98a", "a70ec655b25f68b33885889a0fe26bc1dd4fca65"),
}
TAG_OBJECTS = {"26.1": "9d067c9025d559071ed9f17bfa1f84cde20be82d",
               "26.2": "c41faf07699c587d5b45655bd277b4b12d928395",
               "26.3": "d2664ecbb0e8fad6a1c4a83de7023fd6ba0514ee"}


def digest(path):
    with path.open("rb") as source:
        return hashlib.file_digest(source, "sha256").hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java-home", default=os.environ.get("JAVA_HOME"))
    args = parser.parse_args()
    properties = dict(line.split("=", 1) for line in (ROOT / "gradle.properties").read_text().splitlines()
                      if "=" in line and not line.startswith("#"))
    mc = properties["minecraft_version"]
    version, archive_hash, commit = PINS[mc]
    if properties["yacl_version"] != version:
        raise RuntimeError("YACL dependency version differs from the verified source pin")
    cache = ROOT / ".gradle-agent"
    repository = cache / "yacl-repository"
    work = cache / "yacl-source" / mc
    work.mkdir(parents=True, exist_ok=True)
    archive = work / "upstream.zip"
    tag = "F-" + version
    url = "https://api.github.com/repos/SvyatON/YACL-Reforged/zipball/" + TAG_OBJECTS[mc]
    if not archive.exists():
        temporary = archive.with_suffix(".download")
        request = urllib.request.Request(url, headers={"User-Agent": "AutoMessage-dependency-build"})
        with urllib.request.urlopen(request, timeout=120) as response, temporary.open("wb") as output:
            shutil.copyfileobj(response, output)
        temporary.replace(archive)
    if digest(archive) != archive_hash:
        raise RuntimeError(f"Upstream source SHA-256 mismatch: {archive}")
    with zipfile.ZipFile(archive) as source:
        names = source.namelist()
        top = names[0].split("/")[0]
        for name in names:
            target = (work / name).resolve()
            if not target.is_relative_to(work.resolve()):
                raise RuntimeError("Invalid upstream archive path")
        source.extractall(work)
    upstream = work / top
    init = work / "publish.init.gradle"
    init.write_text("""allprojects {
    plugins.withId('maven-publish') {
        publishing.repositories.maven {
            name = 'AutoMessageLocal'
            url = uri(System.getProperty('automessage.yacl.repository'))
        }
    }
}
""", encoding="utf-8")
    wrapper = upstream / ("gradlew.bat" if os.name == "nt" else "gradlew")
    if os.name != "nt":
        wrapper.chmod(wrapper.stat().st_mode | 0o100)
    command = [str(wrapper), "--gradle-user-home", str(cache), "--init-script", str(init),
               f":{mc}-forge:buildAndCollect", f":{mc}-forge:publishReforgedPublicationToAutoMessageLocalRepository",
               f"-Ptargets={mc}", "--configure-on-demand", "--no-daemon", "--console=plain",
               "-Dautomessage.yacl.repository=" + repository.as_uri()]
    environment = os.environ.copy()
    if args.java_home:
        java_home = str(Path(args.java_home).resolve())
        command.append("-Dorg.gradle.java.home=" + java_home)
        environment["JAVA_HOME"] = java_home
    print(f"Building unmodified YACL Reforged {tag}, source commit {commit}", flush=True)
    subprocess.run(command, cwd=upstream, env=environment, check=True)
    destination = repository / "dev/svyaton/yacl-reforged" / version
    binary = destination / f"yacl-reforged-{version}.jar"
    sources = destination / f"yacl-reforged-{version}-sources.jar"
    for item in (binary, sources, destination / f"yacl-reforged-{version}.pom"):
        if not item.is_file():
            raise RuntimeError(f"Publication missing: {item}")
    with zipfile.ZipFile(binary) as jar:
        metadata = jar.read("META-INF/mods.toml").decode("utf-8")
        if 'modId="yacl_reforged"' not in metadata.replace(" ", ""):
            raise RuntimeError("Unexpected dependency mod ID")
    provenance = {"project": "https://github.com/SvyatON/YACL-Reforged", "tag": tag,
                  "source_commit": commit, "source_url": url, "source_archive_sha256": archive_hash,
                  "license": "LGPL-3.0-or-later", "binary_sha256": digest(binary),
                  "sources_sha256": digest(sources), "built_from_source": True}
    (destination / "provenance.json").write_text(json.dumps(provenance, indent=2) + "\n", encoding="utf-8")
    print(f"Prepared separate dependency: {binary}", flush=True)


if __name__ == "__main__":
    main()
