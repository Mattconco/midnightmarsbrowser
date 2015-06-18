# Developer Documentation #

These are the basic steps to set up the Midnight Mars Browser source code in Eclipse 3.3.  This is only for people who know Java and want to mess with the program source code.  Most people should simply download the program .zip file instead.

## Checking out the source code ##

  1. Install [Subversion](http://subversion.tigris.org/) for your platform
  1. Create a directory for the projects (this will be your Eclipse workspace directory)
  1. cd to that directory at the command line
  1. `svn checkout https://midnightmarsbrowser.googlecode.com/svn/trunk/ . --username yourid@gmail.com`
> > or if you are not a project member, omit the --username option for anonymous checkout

## Setting up the Eclipse projects ##

  1. Run Eclipse 3.3
  1. Switch Workspace your checkout directory from above
  1. File->Import:
    1. select General->Existing Projects into Workspace
    1. select workspace directory
    1. select all projects
    1. Finish

## Running the program from Eclipse ##

  1. Select "Open Run Dialog..."
  1. Select "Eclipse Application"
  1. Press the "New Launch Configuration" button
  1. Give the new configuration a name
  1. Make sure "Run a product" is selected and choose "MidnightMarsBrowser.product"
  1. In the "Arguments" tab, in "VM arguments", put `-Xmx512m -DmaxPanBytes=256m`
  1. Click "Apply" and "Run"