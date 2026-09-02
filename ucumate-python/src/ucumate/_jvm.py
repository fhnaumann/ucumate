"""JVM lifecycle management for the ucumate Python bindings."""

from __future__ import annotations

import os
import threading
from pathlib import Path

_lock = threading.Lock()
_started = False


def _find_jar() -> str:
    """Locate the ucumate fat JAR.

    Resolution order:
    1. UCUMATE_JAR environment variable (full path to JAR).
    2. Any ucumate-python-bundle-*.jar inside this package's jars/ directory.
    """
    env = os.environ.get("UCUMATE_JAR")
    if env:
        if not Path(env).exists():
            raise FileNotFoundError(f"UCUMATE_JAR points to a missing file: {env}")
        return env

    jar_dir = Path(__file__).parent / "jars"
    candidates = sorted(jar_dir.glob("ucumate-python-bundle-*.jar"))
    if candidates:
        return str(candidates[-1])

    raise FileNotFoundError(
        "ucumate JAR not found. Either:\n"
        "  • Set the UCUMATE_JAR env var to the path of the fat JAR, or\n"
        f"  • Place ucumate-python-bundle-*.jar in {jar_dir}\n"
        "Build the fat JAR with:\n"
        "  cd ucumate-python-bundle && mvn package -DskipTests\n"
        "then copy target/ucumate-python-bundle-*.jar into the jars/ directory."
    )


def _find_jvm(jpype) -> str:
    """Locate the JVM, turning JPype's opaque lookup failure into a usable message."""
    try:
        return jpype.getDefaultJVMPath()
    except Exception as exc:
        raise RuntimeError(
            "No Java runtime found. ucumate embeds the ucumate Java library and "
            "needs a Java 21+ JRE or JDK installed.\n"
            "  • Check your installation with: java -version\n"
            "  • If Java is installed but not on PATH, set JAVA_HOME to it.\n"
            "See https://github.com/fhnaumann/ucumate for details."
        ) from exc


def ensure_jvm() -> None:
    """Start the JVM exactly once per process."""
    global _started
    if _started:
        return
    with _lock:
        if _started:
            return
        import jpype
        if not jpype.isJVMStarted():
            jar = _find_jar()
            jpype.startJVM(
                _find_jvm(jpype),
                "-Dorg.slf4j.simpleLogger.defaultLogLevel=warn",
                classpath=[jar],
                convertStrings=False,
            )
        _started = True
