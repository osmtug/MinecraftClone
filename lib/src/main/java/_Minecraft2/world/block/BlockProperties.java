package _Minecraft2.world.block;

import _Minecraft2.Item.tool.ToolType;
import _Minecraft2.render.TextureLoader;

public class BlockProperties {
	public final int upTextureID;
    public final int downTextureID;
    public final int leftTextureID;
    public final int rightTextureID;
    public final int frontTextureID;
    public final int backTextureID;

    public final String upTexturePath;
    public final String downTexturePath;
    public final String leftTexturePath;
    public final String rightTexturePath;
    public final String frontTexturePath;
    public final String backTexturePath;
    public boolean isSolid;
    
    public float breakDuration;
    public ToolType neededTool;
	
    public float width = 2;
    public float height = 2;
    public float depth = 2;
    
    public BlockType type;

    public BlockProperties(
        String upPath, String downPath, String leftPath, String rightPath, String frontPath, String backPath
    ) {
    	isSolid = true;
        this.upTexturePath = upPath;
        this.downTexturePath = downPath;
        this.leftTexturePath = leftPath;
        this.rightTexturePath = rightPath;
        this.frontTexturePath = frontPath;
        this.backTexturePath = backPath;
        
        if (upPath == null ) {
        	downTextureID = 0;
        	leftTextureID = 0;
        	rightTextureID = 0;
        	frontTextureID = 0;
        	backTextureID = 0;
        	upTextureID = 0;
        	return;
        };
        
        downTextureID = TextureLoader.loadTexture(downTexturePath);
		leftTextureID = TextureLoader.loadTexture(leftTexturePath);
		rightTextureID = TextureLoader.loadTexture(rightTexturePath);
		frontTextureID = TextureLoader.loadTexture(frontTexturePath);
		backTextureID = TextureLoader.loadTexture(backTexturePath);
		upTextureID = TextureLoader.loadTexture(upTexturePath);
    }
    
    public BlockProperties(
            String upPath, String downPath, String leftPath
        ) {
            this(upPath, downPath, leftPath, leftPath, leftPath, leftPath);
        }
    
    public BlockProperties(
            String upPath
        ) {
            this(upPath, upPath, upPath, upPath, upPath, upPath);
        }
    
    public BlockProperties(
            String upPath, boolean issolid
        ) {
            this(upPath, upPath, upPath, upPath, upPath, upPath);
            this.isSolid = issolid;
        }
}
