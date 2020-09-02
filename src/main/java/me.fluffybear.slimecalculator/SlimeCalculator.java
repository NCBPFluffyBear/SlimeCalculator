package me.fluffybear.slimecalculator;

import java.util.ArrayList;
import java.util.List;
import me.mrCookieSlime.CSCoreLibPlugin.PluginUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.String.StringUtils;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SlimeCalculator extends JavaPlugin {
    private ArrayList<RecipeType> blackList = new ArrayList<>();

    private ArrayList<String> blackListedIds = new ArrayList<>();

    public void onEnable() {
        PluginUtils utils = new PluginUtils((Plugin)this);
        utils.setupMetrics();
        this.blackList.add(RecipeType.ORE_WASHER);
        this.blackList.add(RecipeType.GOLD_PAN);
        this.blackList.add(RecipeType.MOB_DROP);
        this.blackList.add(RecipeType.ORE_CRUSHER);
        this.blackListedIds.add("_ESSENCE");
        cacheItems();
        getLogger().info("SlimeCalculator v" + getDescription().getVersion() + " has been enabled!");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
        if (cmd.getName().equalsIgnoreCase("calculate") &&
                sender instanceof Player) {
            Player player = (Player)sender;
            if (SlimefunItem.getByItem(player.getInventory().getItemInMainHand()) != null) {
                SlimefunItem item = SlimefunItem.getByItem(player.getInventory().getItemInMainHand());
                int amount = -1;
                if (args.length >= 1)
                    try {
                        amount = Integer.parseInt(args[0]);
                    } catch (NumberFormatException ex) {
                        player.sendMessage(ChatColor.RED + "That's not a valid number!");
                        return true;
                    }
                List<Ingredient> ingredients = Ingredient.getRecipe(item);
                if (ingredients != null) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7-=Recipe for " + ((amount == -1) ? "" : ("x" + amount + " ")) + item.getItem().getItemMeta().getDisplayName() + "&7=-"));
                    int count = 0;
                    for (Ingredient ingredient : ingredients) {
                        ChatColor color = (count == 0) ? ChatColor.YELLOW : ChatColor.GOLD;
                        if (SlimefunItem.getByItem(ingredient.getItem()) != null) {
                            player.sendMessage(color.toString() + "x" + (ingredient.getAmount() * ((amount == -1) ? 1 : amount)) + " " + ChatColor.stripColor(ingredient.getItem().getItemMeta().getDisplayName()));
                        } else {
                            player.sendMessage(color.toString() + "x" + (ingredient.getAmount() * ((amount == -1) ? 1 : amount)) + " " + StringUtils.formatItemName(ingredient.getItem(), false));
                        }
                        count = (count == 0) ? 1 : 0;
                    }
                    return true;
                }
            }
            player.sendMessage(ChatColor.RED + "This is not a Slimefun Item!");
        }
        return true;
    }

    public List<Ingredient> getIngredients(SlimefunItem item, List<Ingredient> ingredientList) {
        for (ItemStack recipeItem : item.getRecipe()) {
            if (SlimefunItem.getByItem(recipeItem) != null) {
                SlimefunItem ingredient = SlimefunItem.getByItem(recipeItem);
                if (ingredient.getRecipe() != null && !this.blackList.contains(ingredient.getRecipeType())) {
                    for (int i = 0; i < recipeItem.getAmount(); i++)
                        getIngredients(ingredient, ingredientList);
                } else if (alreadyExists(new Ingredient(recipeItem, recipeItem.getAmount()), ingredientList)) {
                    for (Ingredient existingIngredient : ingredientList) {
                        if (existingIngredient.getItem().isSimilar(ingredient.getItem()))
                            existingIngredient.addAmount(ingredient.getItem().getAmount());
                    }
                } else {
                    ingredientList.add(new Ingredient(recipeItem, recipeItem.getAmount()));
                }
            } else if (recipeItem != null) {
                if (alreadyExists(new Ingredient(recipeItem, recipeItem.getAmount()), ingredientList)) {
                    for (Ingredient existingIngredient : ingredientList) {
                        if (existingIngredient.getItem().isSimilar(recipeItem))
                            existingIngredient.addAmount(recipeItem.getAmount());
                    }
                } else {
                    ingredientList.add(new Ingredient(recipeItem, recipeItem.getAmount()));
                }
            }
        }
        return ingredientList;
    }

    private boolean alreadyExists(Ingredient ingredient, List<Ingredient> ingredientList) {
        for (Ingredient existingIngredient : ingredientList) {
            if (existingIngredient.getItem().isSimilar(ingredient.getItem()))
                return true;
        }
        return false;
    }

    private void cacheItems() {
        getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, new Runnable() {
            public void run() {
                SlimeCalculator.this.getLogger().info("Starting item caching...");
                long startTime = System.currentTimeMillis();
                for (SlimefunItem item : SlimefunItem.items) {
                    for (String blackListedId : SlimeCalculator.this.blackListedIds) {
                        if (item.getName().contains(blackListedId))
                            continue;
                        Ingredient.addRecipe(item, SlimeCalculator.this.getIngredients(item, new ArrayList<>()));
                    }
                }
                SlimeCalculator.this.getLogger().info("Done Caching - " + (System.currentTimeMillis() - startTime) + "ms");
            }
        }20L);
    }
}
