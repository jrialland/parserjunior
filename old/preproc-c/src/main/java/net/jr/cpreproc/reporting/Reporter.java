package net.jr.cpreproc.reporting;

import net.jr.common.Position;

public interface Reporter {

    void fatal(Position position, String message);

    void error(Position position, String message);

    void info(Position position, String message);

    void warn(Position position, String message);
}
