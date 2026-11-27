"""Run with python3 backend/src/test/typst/test_cv_templates.py (Typst and pdftotext required)."""

import json
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

TEMPLATES = (
    Path(__file__).resolve().parents[2]
    / "main/resources/ch/streckeisen/mycv/backend/templates/cv"
)
JOB = {
    "title": "Lead Engineer",
    "institution": "Example Company",
    "location": "Zurich",
    "startDate": "02.2024",
    "endDate": "Present",
    "description": "Employer description",
}
BREAK = {
    "careerBreak": True,
    "title": "Travel Sabbatical",
    "institution": "Hidden Employer",
    "location": "Hidden City",
    "startDate": "07.2023",
    "endDate": "01.2024",
    "description": "Travelled by train.",
}


class CvTemplateTest(unittest.TestCase):
    def render(self, template, language, entries):
        profile = {
            "language": language,
            "firstName": "Alex",
            "lastName": "Example",
            "jobTitle": "Engineer",
            "bio": None,
            "email": "alex@example.com",
            "phone": "+41 00 000 00 00",
            "address": "Example Street, 8000 Zurich",
            "birthday": "01.01.1990",
            "workExperiences": entries,
            "skills": [],
            "education": [],
            "projects": [],
            "cvStyleOptions": {"bannerBackground": "#000000"},
        }
        with tempfile.TemporaryDirectory() as directory:
            workdir = Path(directory)
            for name in (template, "shared"):
                shutil.copy(TEMPLATES / f"{name}.typ", workdir)
            shutil.copy(Path(__file__).with_name("profile.jpg"), workdir)
            (workdir / "profile.json").write_text(json.dumps(profile), encoding="utf-8")
            subprocess.run(
                ["typst", "compile", f"{template}.typ", "cv.pdf"],
                cwd=workdir, check=True, timeout=60,
            )
            return subprocess.check_output(
                ["pdftotext", "-layout", "cv.pdf", "-"],
                cwd=workdir, text=True, timeout=10,
            )

    def test_mixed_work_history(self):
        for template in ("modern", "talendo"):
            with self.subTest(template=template):
                pdf = self.render(template, "en", [JOB, BREAK])
                for text in ("Career break", "07.2023", "01.2024", BREAK["description"], JOB["institution"]):
                    self.assertIn(text, pdf)
                self.assertLess(pdf.index(JOB["title"]), pdf.index(BREAK["title"]))
                self.assertNotIn(BREAK["institution"], pdf)
                self.assertNotIn(BREAK["location"], pdf)

    def test_break_only_without_description(self):
        for template in ("modern", "talendo"):
            with self.subTest(template=template):
                pdf = self.render(template, "de", [dict(BREAK, description=None, endDate="Heute")])
                for text in ("Berufserfahrung", "Berufliche Auszeit", BREAK["title"], "Heute"):
                    self.assertIn(text, pdf)
                self.assertNotIn(BREAK["description"], pdf)

    def test_legacy_work_history(self):
        for template in ("modern", "talendo"):
            with self.subTest(template=template):
                pdf = self.render(template, "en", [JOB])
                self.assertIn(JOB["title"], pdf)
                self.assertIn(JOB["institution"], pdf)
                self.assertNotIn("Career break", pdf)


if __name__ == "__main__":
    unittest.main()
