package com.yaricktt.heavensdoor.init;

import com.github.standobyte.jojo.JojoMod;
import com.yaricktt.heavensdoor.RotpHDAddon;
import com.yaricktt.heavensdoor.item.PageItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class InitItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RotpHDAddon.MOD_ID);

    public static final RegistryObject<PageItem> PAGE = ITEMS.register("page_item",
            () -> new PageItem(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(32)
                    .tab(JojoMod.MAIN_TAB)
            )
    );
}
