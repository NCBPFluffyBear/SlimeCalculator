package me.fluffybear.slimecalculator;

import java.util.HashMap;
import java.util.List;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import org.bukkit.inventory.ItemStack;

public class Ingredient {
    public static HashMap<SlimefunItem, List<Ingredient>> recipes = new HashMap<>();

    private ItemStack item;

    private int amount;

    public Ingredient(ItemStack item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public int getAmount() {
        return this.amount;
    }

    public void addAmount(int amount) {
        this.amount += amount;
    }

    public static List<Ingredient> getRecipe(SlimefunItem item) {
        if (recipes.containsKey(item))
            return recipes.get(item);
        return null;
    }

    public static void addRecipe(SlimefunItem item, List<Ingredient> ingredients) {
        if (!recipes.containsKey(item) && !ingredients.isEmpty())
            recipes.put(item, ingredients);
    }
}
