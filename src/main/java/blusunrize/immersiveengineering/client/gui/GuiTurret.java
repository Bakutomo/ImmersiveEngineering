package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.client.gui.elements.GuiReactiveList;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTurret;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTurretChem;
import blusunrize.immersiveengineering.common.gui.ContainerTurret;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;

public class GuiTurret extends GuiContainer
{
	public TileEntityTurret tile;
	private GuiTextField nameField;

	public GuiTurret(InventoryPlayer inventoryPlayer, TileEntityTurret tile)
	{
		super(new ContainerTurret(inventoryPlayer, tile));
		this.tile=tile;
		this.ySize = 190;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		this.nameField = new GuiTextField(0, this.fontRendererObj, guiLeft+11,guiTop+88,58,12);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(30);

		this.buttonList.clear();
		this.buttonList.add(new GuiReactiveList(this, 0, guiLeft+10,guiTop+10, 60,72, tile.targetList.toArray(new String[tile.targetList.size()])).setPadding(0,0,2,2).setFormatting(1,false));
		this.buttonList.add(new GuiButtonIE(1, guiLeft+74,guiTop+84, 24,16, "Add", "immersiveengineering:textures/gui/turret.png", 176,65));
		this.buttonList.add(new GuiButtonCheckbox(2, guiLeft+74,guiTop+10, "Blacklist", !tile.whitelist));
		this.buttonList.add(new GuiButtonCheckbox(3, guiLeft+74,guiTop+26, "Animals", tile.attackAnimals));
		this.buttonList.add(new GuiButtonCheckbox(4, guiLeft+74,guiTop+42, "Players", tile.attackPlayers));
		this.buttonList.add(new GuiButtonCheckbox(5, guiLeft+74,guiTop+58, "Neutrals", tile.attackNeutrals));

		if(tile instanceof TileEntityTurretChem)
			this.buttonList.add(new GuiButtonState(6, guiLeft+135,guiTop+68, 14,14, null, ((TileEntityTurretChem)tile).ignite, "immersiveengineering:textures/gui/turret.png",176,51, 0));
	}
	@Override
	protected void actionPerformed(GuiButton button)
	{
		NBTTagCompound tag = new NBTTagCompound();
		int listOffset = -1;
		if(button.id==0)
		{
			int rem = ((GuiReactiveList)button).selectedOption;
			tile.targetList.remove(rem);
			tag.setInteger("remove", rem);
			listOffset = ((GuiReactiveList)button).getOffset()-1;
		}
		else if(button.id==1 && !this.nameField.getText().isEmpty())
		{
			String name = this.nameField.getText();
			if(!tile.targetList.contains(name))
			{
				listOffset = ((GuiReactiveList)buttonList.get(0)).getMaxOffset();
				tag.setString("add", name);
				tile.targetList.add(name);
			}
			this.nameField.setText("");
		}
		else if(button.id==2)
		{
			tile.whitelist = !((GuiButtonState)button).state;
			tag.setBoolean("whitelist", tile.whitelist);
		}
		else if(button.id==3)
		{
			tile.attackAnimals = ((GuiButtonState)button).state;
			tag.setBoolean("attackAnimals", tile.attackAnimals);
		}
		else if(button.id==4)
		{
			tile.attackPlayers = ((GuiButtonState)button).state;
			tag.setBoolean("attackPlayers", tile.attackPlayers);
		}
		else if(button.id==5)
		{
			tile.attackNeutrals = ((GuiButtonState)button).state;
			tag.setBoolean("attackNeutrals", tile.attackNeutrals);
		}
		else if(button.id==6 && tile instanceof TileEntityTurretChem)
		{
			((TileEntityTurretChem)tile).ignite = ((GuiButtonState)button).state;
			tag.setBoolean("ignite", ((TileEntityTurretChem)tile).ignite);
		}
		if(!tag.hasNoTags())
		{
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
			this.initGui();
			if(listOffset>=0)
				((GuiReactiveList)this.buttonList.get(0)).setOffset(listOffset);
		}
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		super.drawScreen(mx, my, partial);
		this.nameField.drawTextBox();

		ArrayList<String> tooltip = new ArrayList<String>();
		if(mx>=guiLeft+158&&mx<guiLeft+165 && my>=guiTop+16&&my<guiTop+62)
			tooltip.add(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" RF");

		if(tile instanceof TileEntityTurretChem)
		{
			ClientUtils.handleGuiTank(((TileEntityTurretChem)tile).tank, guiLeft+134,guiTop+16,16,47, 196,0,20,51, mx,my, "immersiveengineering:textures/gui/turret.png", tooltip);
			if(mx>=guiLeft+135&&mx<guiLeft+149 && my>=guiTop+68&&my<guiTop+82)
				tooltip.add("Ignite Fluid");
		}
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRendererObj, -1,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/turret.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0, 0, xSize, ySize);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+158,guiTop+22+(46-stored), guiLeft+165,guiTop+68, 0xffb51500, 0xff600b00);

		if(tile instanceof TileEntityTurretChem)
		{
			this.drawTexturedModalRect(guiLeft+132,guiTop+14, 176,0, 20,51);
			ClientUtils.handleGuiTank(((TileEntityTurretChem)tile).tank, guiLeft+134,guiTop+16,16,47, 196,0,20,51, mx,my, "immersiveengineering:textures/gui/turret.png",null);
		}

//		for(int i=0; i<tile.patterns.length; i++)
//			if(tile.inventory[18+i]==null && tile.patterns[i].inv[9]!=null)
//			{
//				ItemStack stack = tile.patterns[i].inv[9];
//				GL11.glPushMatrix();
//				GL11.glTranslatef(0.0F, 0.0F, 32.0F);
//				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//				RenderHelper.disableStandardItemLighting();
//				this.zLevel = 200.0F;
//				itemRender.zLevel = 200.0F;
//				FontRenderer font = null;
//				if(stack!=null)
//					font = stack.getItem().getFontRenderer(stack);
//				if(font==null)
//					font = fontRendererObj;
//				itemRender.renderItemAndEffectIntoGUI(stack, guiLeft+27+i*58, guiTop+64);
//				itemRender.renderItemOverlayIntoGUI(font, stack, guiLeft+27+i*58, guiTop+64, TextFormatting.GRAY.toString()+stack.stackSize);
//				this.zLevel = 0.0F;
//				itemRender.zLevel = 0.0F;
//
//
//				GL11.glDisable(GL11.GL_LIGHTING);
//				GL11.glDisable(GL11.GL_DEPTH_TEST);
//				ClientUtils.drawColouredRect(guiLeft+27+i*58, guiTop+64, 16,16, 0x77444444);
//				GL11.glEnable(GL11.GL_LIGHTING);
//				GL11.glEnable(GL11.GL_DEPTH_TEST);
//
//				GL11.glPopMatrix();
//			}
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		if(this.nameField.isFocused() && keyCode==28)
		{
			String name = this.nameField.getText();
			if(!tile.targetList.contains(name))
			{
				NBTTagCompound tag = new NBTTagCompound();
				tag.setString("add", name);
				tile.targetList.add(name);
				ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));

				this.initGui();
				((GuiReactiveList)this.buttonList.get(0)).setOffset(((GuiReactiveList)this.buttonList.get(0)).getMaxOffset());
			}
		}
		else if(!this.nameField.textboxKeyTyped(typedChar, keyCode))
			super.keyTyped(typedChar, keyCode);

	}
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
	}
}