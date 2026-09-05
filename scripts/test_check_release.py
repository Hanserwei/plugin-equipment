import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location("check_release", Path(__file__).with_name("check-release.py"))
assert spec is not None and spec.loader is not None
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


class ReleaseVersionTest(unittest.TestCase):
    def check(self, tag, version="2.0.0", plugin_name="han-equipment", manifest_version=None):
        return release.check_release(
            tag,
            f"version={version}\n",
            f"metadata:\n  name: {plugin_name}\nspec:\n  version: {manifest_version or version}\n",
        )

    def test_valid_stable_and_prerelease_versions(self):
        for version in ("2.0.0", "2.1.0-rc.1", "3.0.0-beta.2+build.5"):
            for tag in (version, f"v{version}"):
                with self.subTest(tag=tag):
                    self.assertEqual(self.check(tag, version), version)

    def test_rejects_invalid_or_shell_sensitive_tags(self):
        for tag in ("", "latest", "v2.0", "v02.0.0", "v2.0.0-01", "v2.0.0;echo bad", "$(id)", "v2.0.0\n"):
            with self.subTest(tag=tag), self.assertRaises(ValueError):
                self.check(tag)

    def test_rejects_a_tag_for_a_different_commit_version(self):
        with self.assertRaisesRegex(ValueError, "same version"):
            self.check("v2.1.0")

    def test_rejects_inconsistent_manifest_version(self):
        with self.assertRaisesRegex(ValueError, "same version"):
            self.check("v2.0.0", manifest_version="1.1.1")

    def test_rejects_upstream_plugin_identity(self):
        with self.assertRaisesRegex(ValueError, "metadata.name"):
            self.check("v2.0.0", plugin_name="equipment")

    def test_rejects_missing_metadata(self):
        with self.assertRaisesRegex(ValueError, "missing"):
            release.check_release("v2.0.0", "", "")


if __name__ == "__main__":
    unittest.main()
