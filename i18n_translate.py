#!/usr/bin/env python3
import os
import sys
import html
import re

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
TEMPLATES_DIR = os.path.join(BASE_DIR, ".homewebcom", "templates", "native")
EN_TEMPLATES_DIR = os.path.join(BASE_DIR, ".homewebcom", "templates", "native_en")

SUBDIRS = ["console", "pages", "script", "template", "mail", "style", "design"]

HTML_EXTENSIONS = {".html", ".txt"}

from argostranslate.translate import get_translation_from_codes

translation = get_translation_from_codes("de", "en")

def has_german_content(filepath):
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
        if re.search(r'&[a-zA-Z]+;', content):
            return True
        if re.search(r'[\u00C0-\u017F]', content):
            return True
        return False
    except Exception:
        return False

def translate_text_with_vars(text):
    # Use a split approach to preserve variables exactly as they are while translating the surrounding text.
    # This handles complex cases like %prev% %pages% %next% correctly by treating each %...% block as a token.
    var_pattern = r'(%[^%]+%)'
    parts = re.split(var_pattern, text)

    translated_parts = []
    for part in parts:
        if re.match(var_pattern, part):
            # This is a variable, keep it as is
            translated_parts.append(part)
        else:
            # This is regular text, translate it
            stripped = part.strip()
            if not stripped:
                translated_parts.append(part)
            else:
                translated = translation.translate(stripped)
                if not translated:
                    translated = stripped
                # Preserve leading/trailing whitespace of the original part
                leading = part[:len(part) - len(part.lstrip())]
                trailing = part[len(part.rstrip()):]
                translated_parts.append(leading + translated + trailing)

    return "".join(translated_parts)

def translate_html_content(content):
    content = content.replace('type="submit"class=', 'type="submit" class=')
    content = content.replace('type="submit"class=', 'type="submit" class=')
    parts = re.split(r'(<[^>]+>)', content)
    result_parts = []
    for part in parts:
        if part.startswith('<') and part.endswith('>'):
            # Translate value, label, aria-label, title, and placeholder attributes
            for attr in ['value', 'label', 'aria-label', 'title', 'placeholder']:
                pattern = rf'{attr}=["\']([^"\']*)["\']'
                matches = list(re.finditer(pattern, part))
                for m in reversed(matches):
                    original_val = m.group(1)
                    translated_val = translate_text_with_vars(original_val)
                    part = re.sub(rf'{attr}=["\']{re.escape(original_val)}["\']', f'{attr}="{translated_val}"', part, count=1)
            result_parts.append(part)
        else:
            stripped = part.strip()
            if not stripped:
                result_parts.append(part)
                continue
            translated = translate_text_with_vars(part)
            result_parts.append(translated)
    result = ''.join(result_parts)
    result = result.replace('value="Senden!"', 'value="Submit"')
    result = result.replace('value="Send!"', 'value="Submit"')
    result = result.replace('value="&Auml;nderung abschicken!!"', 'value="Submit Changes!!"')
    return result

def process_file(src_path, dst_path, rel_path):
    try:
        with open(src_path, "rb") as f:
            raw = f.read()
    except Exception as e:
        print(f"  ERROR reading {src_path}: {e}", file=sys.stderr)
        return False

    ext = os.path.splitext(src_path)[1].lower()
    filename = os.path.basename(src_path)

    if ext == ".css":
        try:
            os.makedirs(os.path.dirname(dst_path), exist_ok=True)
            with open(dst_path, "wb") as f:
                f.write(raw)
        except Exception as e:
            print(f"  ERROR writing {dst_path}: {e}", file=sys.stderr)
            return False
        return True

    if ext == ".js" and filename != "rooms.js":
        try:
            os.makedirs(os.path.dirname(dst_path), exist_ok=True)
            with open(dst_path, "wb") as f:
                f.write(raw)
        except Exception as e:
            print(f"  ERROR writing {dst_path}: {e}", file=sys.stderr)
            return False
        return True

    try:
        content = raw.decode("utf-8")
    except UnicodeDecodeError:
        content = None

    if content is not None:
        content = html.unescape(content)

        if ext in HTML_EXTENSIONS or filename == "rooms.js":
            content = translate_html_content(content)

        try:
            os.makedirs(os.path.dirname(dst_path), exist_ok=True)
            with open(dst_path, "w", encoding="utf-8") as f:
                f.write(content)
        except Exception as e:
            print(f"  ERROR writing {dst_path}: {e}", file=sys.stderr)
            return False
    else:
        try:
            os.makedirs(os.path.dirname(dst_path), exist_ok=True)
            with open(dst_path, "wb") as f:
                f.write(raw)
        except Exception as e:
            print(f"  ERROR writing {dst_path}: {e}", file=sys.stderr)
            return False

    return True

def main():
    if not os.path.isdir(TEMPLATES_DIR):
        print(f"Source directory does not exist: {TEMPLATES_DIR}", file=sys.stderr)
        sys.exit(1)

    os.makedirs(EN_TEMPLATES_DIR, exist_ok=True)

    total_files = 0
    translated_files = 0
    skipped_files = 0

    for subdir in SUBDIRS:
        src_subdir = os.path.join(TEMPLATES_DIR, subdir)
        dst_subdir = os.path.join(EN_TEMPLATES_DIR, subdir)

        if not os.path.isdir(src_subdir):
            continue

        for root, dirs, files in os.walk(src_subdir):
            for filename in sorted(files):
                src_path = os.path.join(root, filename)
                rel_path = os.path.relpath(src_path, TEMPLATES_DIR)
                dst_path = os.path.join(EN_TEMPLATES_DIR, rel_path)

                total_files += 1

                print(f"Processing: {rel_path}")
                if process_file(src_path, dst_path, rel_path):
                    translated_files += 1
                    print(f"  -> OK")
                else:
                    print(f"  -> FAILED", file=sys.stderr)

    print(f"\nDone! Total: {total_files}, Translated: {translated_files}, Skipped: {skipped_files}")

if __name__ == "__main__":
    main()