"""Validate the actual self-contained Forge release archive, including nested YACL."""
import hashlib
import io
import json
from pathlib import Path
import tomllib
import zipfile

root = Path(__file__).resolve().parents[1]
properties = dict(line.split('=', 1) for line in (root / 'gradle.properties').read_text(encoding='utf-8').splitlines()
                  if '=' in line and not line.startswith('#'))
version = properties['yacl_version']
artifact = root / f"build/libs/rss-automessage-forge-{properties['mod_version']}.jar"
dependency = root / '.gradle-agent/yacl-repository/dev/svyaton/yacl-reforged' / version / f'yacl-reforged-{version}.jar'
with zipfile.ZipFile(artifact) as jar:
    assert jar.testzip() is None
    names = jar.namelist()
    assert not any('ForgeSmoke' in name or 'icon720-450' in name for name in names)
    metadata = json.loads(jar.read('META-INF/jarjar/metadata.json'))
    entries = metadata['jars']
    assert len(entries) == 2, 'Expect embedded YACL and the Forge-provided MixinExtras requirement'
    entry = next(item for item in entries if item['identifier']['artifact'] == 'yacl-reforged')
    assert entry['identifier'] == {'group': 'dev.svyaton', 'artifact': 'yacl-reforged'}
    assert entry['version']['artifactVersion'] == version
    assert entry['version']['range'] == f'[{version}]'
    assert entry['path'].startswith('META-INF/jarjar/')
    mixinextras = next(item for item in entries if item['identifier']['artifact'] == 'mixinextras-forge')
    assert mixinextras['identifier']['group'] == 'io.github.llamalad7'
    assert mixinextras['version']['range'] == f"[{properties['mixinextras_version']},)"
    assert len([name for name in names if name.endswith('.jar')]) == 1
    nested = jar.read(entry['path'])
    assert hashlib.sha256(nested).digest() == hashlib.sha256(dependency.read_bytes()).digest(), 'Embedded YACL must be the unchanged verified build'
    mods = tomllib.loads(jar.read('META-INF/mods.toml').decode('utf-8'))
    required = {item['modId']: item for item in mods['dependencies']['rss_automessage']}
    assert required['yacl_reforged']['versionRange'] == f'[{version}]'
    assert 'META-INF/THIRD-PARTY-NOTICES.md' in names
    with zipfile.ZipFile(io.BytesIO(nested)) as yacl:
        assert yacl.testzip() is None
        mod = tomllib.loads(yacl.read('META-INF/mods.toml').decode('utf-8'))['mods'][0]
        assert mod['modId'] == 'yacl_reforged' and mod['version'] == version
        assert 'dev/isxander/yacl3/api/YetAnotherConfigLib.class' in yacl.namelist()
        for notice in ['LICENSE', 'NOTICE', 'MODIFICATIONS.md', 'THIRD-PARTY-LICENSES.md']:
            assert f'META-INF/{notice}' in yacl.namelist(), notice
        children = json.loads(yacl.read('META-INF/jarjar/metadata.json'))['jars']
        assert len(children) == 9
        for child in children:
            with zipfile.ZipFile(io.BytesIO(yacl.read(child['path']))) as library:
                assert library.testzip() is None
print(f"PASS: self-contained Forge {properties['minecraft_version']} archive; exact YACL + 9 original libraries; licenses retained; {artifact.stat().st_size} bytes")
