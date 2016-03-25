package legacy.com.galacticwars;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by IIS on 3/4/2016.
 */
public class Image {

    protected float[] vertices = {
            -1,1,0,
            -1,-1,0,
            1,-1,0,
            1,1,0

    };


    protected short[] drawOrder = {1,2,3,0,2,3};

    private final String vertexShadercode = "uniform mat4 uMVPMatrix;" + "attribute vec4 vPosition;" +
                                            "attribute vec2 TexCoordIn;" +
                                            "varying vec2 TexCoordOut;" +
                                            "void main() { " +
                                            "gl_Position = uMVPMatrix * vPosition;" +
                                            "TexCoordOut = TexCoordIn;" +
                                            "}";

    private final String fragmentShaderCode = "Precision mediump float;" +
                                            "uniform vec4 vColor;" +
                                            "uniform sampler2D TexCoordIn;" +
                                            "varying vec2 TexCoordOut;" +
                                            "void main() {" +
                                            "gl_FragColor = texture2D(TexCoordIn, vec2(TexCoord.x, TexCoord.y));" +
                                            "}";

    protected float[] texture = {
            -1f, 1f,
            -1f,-1f,
            1f, -1f,
            1f,1f,
    };

    private int[] textures = new int[1];
    private  FloatBuffer vertexBuffer, texBuff;
    private  ShortBuffer pointBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private int COORDS_PER_VERT = 3;
    private int COORDS_PER_TEX = 2;

    public Image(){

        ByteBuffer vertexBytes = ByteBuffer.allocateDirect(vertices.length * 4);
        vertexBytes.order(ByteOrder.nativeOrder());
        vertexBuffer = vertexBytes.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer pointBytes = ByteBuffer.allocateDirect(drawOrder.length * 2);
        pointBytes.order(ByteOrder.nativeOrder());
        pointBuffer = pointBytes.asShortBuffer();
        pointBuffer.put(drawOrder);
        pointBuffer.position(0);

        ByteBuffer textureBuffer = ByteBuffer.allocateDirect(texture.length * 4);
        textureBuffer.order(ByteOrder.nativeOrder());
        texBuff = textureBuffer.asFloatBuffer();
        texBuff.put(texture);
        texBuff.position(0);


        int vertexShader = RenderClass.loadShader(GLES20.GL_VERTEX_SHADER, vertexShadercode);
        int fragShader = RenderClass.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragShader);
        GLES20.glLinkProgram(mProgram);

    }

    public void draw(float[] mvpMatrix){
        GLES20.glUseProgram(mProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        int vTextureCoord = GLES20.glGetAttribLocation(mProgram, "TexCoordIn");
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERT, GLES20.GL_FLOAT, false, COORDS_PER_VERT * 4, vertexBuffer);
        GLES20.glVertexAttribPointer(vTextureCoord, COORDS_PER_TEX, GLES20.GL_FLOAT, false, COORDS_PER_TEX * 4, texBuff);
        GLES20.glEnableVertexAttribArray(vTextureCoord);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        int fsTexture = GLES20.glGetUniformLocation(mProgram, "TexCoordOut");
        GLES20.glUniform1i(fsTexture, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
//        RenderClass.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
      //  RenderClass.checkGlError("glUniformMatrix4fv");
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, pointBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public  void loadTexture(int texture, Context context){
        InputStream imageStream = context.getResources().openRawResource(texture);
        Bitmap bitmap = null;

        android.graphics.Matrix  flip = new android.graphics.Matrix();
        flip.postScale(-1f, -1f);
         try {
             bitmap = BitmapFactory.decodeStream(imageStream);

         }catch (Exception e){

         }finally {
             try {
                 imageStream.close();
                 imageStream = null;
             } catch (IOException e) {
                 e.printStackTrace();
             }

         }

        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        
    }


}