# Added support for downloading Phoenix images

# Downloading Phoenix images #

I've updated MMB 2.0 to add a basic utility for downloading Phoenix images. Two new menu options are under the Update menu: "Fast Update Phoenix Images" and "Full Update Phoenix Images". Both options are similar, they download images from http://phoenix.lpl.arizona.edu/images/gallery/. As the name suggests, "Fast Update" is faster and I suggest you use that one most of the time. When you run them, the output in the Update Console explains what happens.

This is just to help with downloading files; there is no support for Phoenix Slideshows or Panorama views as there is with MER. This is because the two missions are really very different and it would be more work than I have time for to expand MMB 2.0 to accommodate Phoenix. Phoenix really requires a special program of it's own. I might try making one sometime (it's really really tempting), but don't be surprised if it's only for Mac this time, or if it never happens at all.

The JPGs are stored under {mmbworkspace}/Phoenix/jpg and the files filenames are there are lg\_xxx.jpg, to match the filenames used on the phoenix.lpl.arizona.edu site. The downloaded images in the "jpg" directory should be left alone, don't rename or modify them, because the downloader checks these files to see what needs to get updated.

However, since people seem to prefer the files to be renamed to match the product ID, the program also copies the files to a directory named "renamed", where they are (you guessed it) renamed to match the product ID. You can delete the renamed files if you don't want them. Information on how to decode the filenames can be found here: http://www.met.tamu.edu/mars/filenames.html.

Newly downloaded images are also copied (and renamed) to a folder under {mmbworkspace}/Phoenix/inbox/{timestamp}, so you can see what images have just downloaded using Windows Explorer or the Mac OS Finder.

Some metadata is embedded in the jpg images on the phoenix.lpl.arizona.edu site. You can open the downloaded jpgs in a text editor and view any metadata information that's there.  Also, at the end of every Phoenix image update, MMB scans all the Phoenix images in the jpg folder for metadata, and dumps all the raw image metadata in CSV format to the file {mmbdir}/Phoenix/metadata.csv.