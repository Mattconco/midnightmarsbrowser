# Increasing Memory for the Panorama Feature #

In 2.0 you can adjust the maximum memory used for panorama images as well as the maximum memory used for the whole application.  When a panorama would use more than the allowed maximum panorama memory, the resolution is decreased until the panorama can be shown using the allowed amount memory.

## On Windows ##

Edit the MidnightMarsBrowser.ini file using Notepad or WordPad.  The line

`    -Xmx256m`

controls the maximum memory available to the program; change it to

`-Xmx512m`

for example. The line

`    -DmaxPanBytes=150m`

controls the maximum memory used for panorama images.  Change it to a higher number, such as

`-DmaxPanBytes=300m`

for example.  This number must be lower than the maximum memory, to leave space for the program to operate.

## On Max OS X ##

The instructions here are similar to the Windows instructions, except you need to edit the MidnightMarsBrowser.ini file within the application bundle, not the one next to  the application. You will also need to add a line for the the maxPanBytes parameter in the file.

Select the MidnightMarsBrowser application in the Finder, control-click and select “Show Package Contents”. Navigate to Contents/MacOS and edit the MidnightMarsBrowser.ini file there, using Text. Change the maximum memory parameter line `-Xmx256m` to `-Xmx512m` for example, as in the Windows instructions above.  Add a line after the `-Xmx512m` line like this:

`   -DmaxPanBytes=150m`

... and change the number to 300, for example.