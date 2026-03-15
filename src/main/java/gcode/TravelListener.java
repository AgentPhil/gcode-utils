package gcode;

import gcode.PrintHead.Travel;

public interface TravelListener {
	void handle(Travel travel);
}
