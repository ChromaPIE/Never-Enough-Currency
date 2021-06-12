package com.zundrel.currency.common.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import com.zundrel.currency.Currency;
import com.zundrel.currency.common.capabilities.AccountCapability;
import com.zundrel.currency.common.capabilities.CartCapability;
import com.zundrel.currency.common.config.ConfigHandler;
import com.zundrel.currency.common.info.ModInfo;
import com.zundrel.currency.common.items.ItemHandler;
import com.zundrel.currency.common.items.ItemWallet;
import com.zundrel.currency.common.utils.CurrencyUtils;

@Mod.EventBusSubscriber(modid = ModInfo.MODID)
public class ModEventHandler {
	@SubscribeEvent
	public static void onLivingDropsEvent(LivingDropsEvent event) {
		if (ConfigHandler.dropMoney && !(event.getEntityLiving() instanceof EntityPlayer) && event.getEntityLiving() instanceof IMob && event.getEntityLiving().getEntityWorld().isRemote == false) {
			if (event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof EntityPlayer && !(event.getSource().getTrueSource() instanceof FakePlayer)) {
				CurrencyUtils.dropMoneyAmount(event.getEntityLiving().getMaxHealth() / ConfigHandler.mobDivisionValue, event.getEntityLiving().getEntityWorld(), event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ);
				return;
			}

			if (event.getSource().getTrueSource() != null && event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof EntityArrow) {
				EntityArrow arrow = (EntityArrow) event.getSource().getTrueSource();
				if (arrow.shootingEntity instanceof EntityPlayer && !(arrow.shootingEntity instanceof FakePlayer)) {
					CurrencyUtils.dropMoneyAmount(event.getEntityLiving().getMaxHealth() / ConfigHandler.mobDivisionValue, event.getEntityLiving().getEntityWorld(), event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ);
					return;
				}
			}
		}
	}

	@SubscribeEvent
	public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(new ResourceLocation(ModInfo.MODID, "account"), new AccountCapability((EntityPlayer) event.getObject()));
			event.addCapability(new ResourceLocation(ModInfo.MODID, "cart"), new CartCapability((EntityPlayer) event.getObject()));
		}
	}

	@SubscribeEvent
	public static void onPlayerCloned(PlayerEvent.Clone event) {
		if (event.getOriginal().hasCapability(Currency.ACCOUNT_DATA, null)) {
			AccountCapability cap = event.getOriginal().getCapability(Currency.ACCOUNT_DATA, null);
			AccountCapability newCap = event.getEntityPlayer().getCapability(Currency.ACCOUNT_DATA, null);
			newCap.setAmount(cap.getAmount(), true);
		}

		if (event.getOriginal().hasCapability(Currency.CART_DATA, null)) {
			CartCapability cap = event.getOriginal().getCapability(Currency.CART_DATA, null);
			CartCapability newCap = event.getEntityPlayer().getCapability(Currency.CART_DATA, null);
			newCap.setCart(cap.getCart(), true);
		}
	}

	@SubscribeEvent
	public static void onPlayerJoined(PlayerLoggedInEvent event) {
		if (event.player.hasCapability(Currency.ACCOUNT_DATA, null)) {
			AccountCapability cap = event.player.getCapability(Currency.ACCOUNT_DATA, null);
			cap.sendPacket();
		}

		if (event.player.hasCapability(Currency.CART_DATA, null)) {
			CartCapability cap = event.player.getCapability(Currency.CART_DATA, null);
			cap.sendPacket();
		}
	}

	@SubscribeEvent
	public static void onPlayerRespawn(PlayerRespawnEvent event) {
		if (event.player.hasCapability(Currency.ACCOUNT_DATA, null)) {
			AccountCapability cap = event.player.getCapability(Currency.ACCOUNT_DATA, null);
			cap.sendPacket();
		}

		if (event.player.hasCapability(Currency.CART_DATA, null)) {
			CartCapability cap = event.player.getCapability(Currency.CART_DATA, null);
			cap.sendPacket();
		}
	}
}
