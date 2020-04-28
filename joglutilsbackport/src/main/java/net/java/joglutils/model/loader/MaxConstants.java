/*
 * MaxConstants.java
 *
 * Created on February 12, 2008, 10:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.java.joglutils.model.loader;

/**
 * Constants representing block types in the 3DS file.  This is not a complete
 * list; it only includes the types that are currently recognized.  They are
 * all of type <code>int</code> since 3DS files allocate 16 bits to the
 * block type field.
 *
 * @author RodgersGB
 * @version $Revision: 1.0 $
 */
interface MaxConstants {
    // 0xMISC Administrative codes
    public static final int TYPE_3DS_FILE         = 0x4D4D;
    public static final int TYPE_3DS_VERSION      = 0x0002;
    public static final int TYPE_MESH_DATA        = 0x3D3D;
    public static final int TYPE_MESH_VERSION     = 0x3D3E;

    // 0x0--- Data type codes
    public static final int TYPE_COLOR_I          = 0x0011;
    public static final int TYPE_COLOR_F          = 0x0010;
    public static final int TYPE_COLOR_LIN_I      = 0x0012;
    public static final int TYPE_COLOR_LIN_F      = 0x0013;
    public static final int TYPE_PERCENT_I        = 0x0030;
    public static final int TYPE_PERCENT_F        = 0x0031;

    // 0x1--- Background codes
    public static final int TYPE_BG_BITMAP        = 0x1100;
    public static final int TYPE_BG_USE_BITMAP    = 0x1101;
    public static final int TYPE_BACKGROUND_COLOR = 0x1200;
    public static final int TYPE_BG_USE_SOLID     = 0x1201;
    public static final int TYPE_BG_GRADIENT      = 0x1300;
    public static final int TYPE_BG_USE_GRADIENT  = 0x1301;

    // 0x2--- Ambient light/fog code
    public static final int TYPE_AMBIENT_COLOR    = 0x2100;
    public static final int TYPE_FOG              = 0x2200;
    public static final int TYPE_USE_FOG          = 0x2201;
    public static final int TYPE_FOG_BGND         = 0x2210;
    public static final int TYPE_LAYER_FOG        = 0x2302;
    public static final int TYPE_USE_LAYER_FOG    = 0x2302;

    // 0x3--- View codes

    // 0x4--- Object data codes
    public static final int TYPE_NAMED_OBJECT     = 0x4000;
    public static final int TYPE_TRIANGLE_OBJECT  = 0x4100;
    public static final int TYPE_POINT_LIST       = 0x4110;
    public static final int TYPE_VERTEX_OPTIONS   = 0x4111;
    public static final int TYPE_FACE_LIST        = 0x4120;
    public static final int TYPE_MAT_FACE_LIST    = 0x4130;
    public static final int TYPE_MAT_UV           = 0x4140;
    public static final int TYPE_SMOOTH_GROUP     = 0x4150;
    public static final int TYPE_MESH_MATRIX      = 0x4160;
    public static final int TYPE_MESH_COLOR       = 0x4165;
    // 0x46-- Light data codes
    public static final int TYPE_DIRECT_LIGHT     = 0x4600;
    public static final int TYPE_SPOTLIGHT        = 0x4610;
    public static final int TYPE_ATTENUATION      = 0x4625;
    public static final int TYPE_AMBIENT_LIGHT    = 0x4680;
    // 0x47-- Camera data codes
    public static final int TYPE_CAMERA           = 0x4700;

    // 0x5--- Unknown shape stuff

    // 0x6--- Path/curve codes

    // 0x7--- Viewport codes

    // 0x8--- Unknown XDATA codes

    // 0xA--- Material codes
    public static final int TYPE_MATERIAL         = 0xAFFF;
    public static final int TYPE_MATERIAL_NAME    = 0xA000;
    public static final int TYPE_MAT_AMBIENT      = 0xA010;
    public static final int TYPE_MAT_DIFFUSE      = 0xA020;
    public static final int TYPE_MAT_SPECULAR     = 0xA030;
    public static final int TYPE_MAT_SHININESS    = 0xA040;
    public static final int TYPE_MAT_SHININESS2   = 0xA041;
    public static final int TYPE_MAT_TRANSPARENCY = 0xA050;
    public static final int TYPE_MAT_XPFALL       = 0xA052;
    public static final int TYPE_MAT_REFBLUR      = 0xA053;
    public static final int TYPE_MAT_2_SIDED      = 0xA081;
    public static final int TYPE_MAT_SELF_ILPCT   = 0xA084;
    public static final int TYPE_MAT_SHADING      = 0xA100;
    // 0xA2+- Texture codes
    public static final int TYPE_MAT_TEXMAP       = 0xA200;
    public static final int TYPE_MAT_MAPNAME      = 0xA300;

    // 0xB--- Node codes
    public static final int TYPE_KEY_FRAME       = 0xB000;
    public static final int TYPE_FRAME_INFO      = 0xB002;
    public static final int TYPE_FRAMES          = 0xB008;
    public static final int TYPE_PIVOT_POINT     = 0xB020;


    // 0xC--- Unknown codes
    // 0xD--- Unknown codes
    // 0xF--- Unknown codes
}