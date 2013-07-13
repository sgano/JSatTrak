Mercator tiled image layers
---------------------------
Contribution from WWJ Forum member 'Omega' - February 2009: 
http://forum.worldwindcentral.com/showthread.php?t=20978

Some examples from WWJ Forum member 'vash'.


Package
-------
experimental.gov.nasa.worldwind.layers.mercator


Description
-----------

This extension is essentially a modification of TiledImageLayer and BasicTiledImageLayer 
to handle Mercator projected tiled image layers such as the one commonly used by Google Maps, 
Yahoo Maps, MS Virtual Earth or Open Street Map.

Example layers targeting the above services - except GM, are gathered in the examples sub package.

To try out some of the example layers, add these lines to the default layer list in 
src.config.worldwind.prperties:

  ,gov.nasa.worldwind.layers.mercator.examples.YahooMapsLayer\
  ,gov.nasa.worldwind.layers.mercator.examples.VirtualEarthLayer\
  ,gov.nasa.worldwind.layers.mercator.examples.OSMCycleMapLayer\
  ,gov.nasa.worldwind.layers.mercator.examples.OSMMapnikLayer\


Notes
-----

from Patrick Murris, Feb. 11, 2009

There are a couple interesting features and ideas:

1. Allow the tiled image layer classes to be more generic - less tied to the lat-lon projection, 
and easily extensible, to avoid duplicating code like in this contribution.

2. This implementation includes an image post processing step that can be overrided by subclasses. 
In this instance it allows a somewhat small reprojection of the image, but also allows to add 
transparency to some parts of the image - see the example OSMMapnikTransparentLayer, or test 
if the tile is all white to mark it as absent instead of overwriting the previous level whith 
white - see the examples.MSVirtualEarthLayer.

3. It exposes the 'splitScale' property which determines when the layer does switch from one 
level to the other. Apparently it was motivated by the tile size those services commonly use: 256 
instead of 512. A larger 'splitScale' was necessary to avoid blurred levels.

That points to a weakness in the current implementation - see WWJ-120, where level selection does 
not account properly for the projected tile size, when the viewport dimension or field of view 
change for instance. 
