better-than-xml
===============

A collection of implementations of my personal BTX file format.

In an attempt to at least show the silliness of the XML format, I've decided to implement a near-clone of it that is better in many ways, with the one negative that it won't be 100% human-readable/editable. I plan on creating a simple editor that mitigates that though.

In order to spread awareness, I will implement it in as many programming languages that I know. People who wish to help out are definitely welcome. Any programming languages that are not already represented are encouraged!

The File Format
---------------

There is initially one version of the BTX file format, but as time goes on it may evolve more. Because of that, the first byte is an identifier of the specific version of the file. Right now there is no other metadata for the file, but of course other versions may have subsequent bytes as more info.

In the actual payload of the file, there are three data types. They may be represented differently in different format versions:

<table>
<tr>
<th>Type</th><th>Version '0' definition</th>
</tr>
<tr>
<td>STRING</td>
<td>unsigned 32-bit integer followed by actual data. The integer designates how long the data is (not including itself). In the spirit of XML this would be text (ASCII, unicode, etc) but in reality it could be anything, even binary data. Implementations should provide methods to get raw bytes from STRINGs as well as convenience methods to get data in other formats.</td>
</tr><tr>
<td>ATTRIBUTE</td>
<td>One STRING representing the name of the attribute and a single byte representing metadata about the property. Currently, only two metadata values are supported: a value of 0, which represents that the attribute has a value of NULL (null, nil, None, etc) and a value of 1, which represents that the attribute has a value. In the case that the metadata is 1, the value will follow directly after as a STRING.</td>
</tr><tr>
<td>OBJECT</td>
<td>There are five elements to every OBJECT:
<ul>
<li>One STRING which describes the object (eg the name of it)</li>
<li>An unsigned 32-bit integer describing the number of attributes this OBJECT has</li>
<li>An unsigned 32-bit integer describing the number of OBJECT children this OBJECT contains</li>
<li>A list of attributes</li>
<li>A list of child OBJECTS
</ul>
</td>
</tr>
</table>

In addition, different file versions may have different implementation details:
<table>
<tr><th>Version</th><th>Byte Identifier</th><th>Details</th></tr>
<tr><td>'0'</td><td>0x00</td>
<td><ul>
<li>All references to unsigned integers are in big-endian format</li>
<li>At the root of the BTX file, after the version byte, there is an unsigned 32-bit integer that the count of root objects in this file. This exists because it doesn't force you have a single root object to contain others if you just want to make a list.</li>
</ul></td></tr>
</table>
