# -*- coding: utf-8 -*-
import os
import sys
import subprocess

# List of channels to generate
CHANNELS = ["yingyongbao", "huawei", "coolapk"]

def generate_channel_apks(base_apk_path):
    """
    Generates channel APKs using walle-cli.

    :param base_apk_path: Path to the base APK file.
    """
    if not os.path.exists(base_apk_path):
        print(f"Error: Base APK not found at '{base_apk_path}'")
        sys.exit(1)

    output_dir = os.path.join(os.path.dirname(base_apk_path), 'channel_apks')
    os.makedirs(output_dir, exist_ok=True)

    print(f"Generating channel APKs for: {', '.join(CHANNELS)}")

    for channel in CHANNELS:
        channel_apk_path = os.path.join(output_dir, f"app-{channel}-release.apk")
        command = [
            "walle-cli",
            "put",
            "-c",
            channel,
            base_apk_path,
            channel_apk_path
        ]
        
        print(f"Running command: {' '.join(command)}")
        try:
            subprocess.run(command, check=True, capture_output=True, text=True)
            print(f"Successfully generated: {channel_apk_path}")
        except FileNotFoundError:
            print("\nError: 'walle-cli' command not found.")
            print("Please install Walle CLI: pip install walle-cli")
            sys.exit(1)
        except subprocess.CalledProcessError as e:
            print(f"\nError generating channel APK for '{channel}':")
            print(e.stderr)
            sys.exit(1)

    print(f"\nAll channel APKs have been generated in '{output_dir}'")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python generate_channel_walle.py <path_to_base_apk>")
        sys.exit(1)
    
    apk_path = sys.argv[1]
    generate_channel_apks(apk_path)