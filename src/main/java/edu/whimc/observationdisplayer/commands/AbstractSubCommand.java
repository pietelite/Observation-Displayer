package edu.whimc.observationdisplayer.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.utils.Utils;

public abstract class AbstractSubCommand {

    private static final String PRIMARY = "&7";
    private static final String SECONDARY = "&b";
    private static final String ACCENT = "&3";
    private static final String SEPARATOR = "&8";
    private static final String TEXT = "&f";

    protected ObservationDisplayer plugin;
    private String baseCommand;
    private String subCommand;
    private Permission permission;

    private String description = "";
    private List<String[]> arguments = new ArrayList<>();
    private int minArgs = 0;
    private int maxArgs = 0;
    private boolean bypassArgumentChecks = false;
    private boolean requiresPlayer = false;

    public AbstractSubCommand(ObservationDisplayer plugin, String baseCommand, String subCommand) {
        this.plugin = plugin;
        this.baseCommand = baseCommand;
        this.subCommand = subCommand;

        String permStr = ObservationDisplayer.PERM_PREFIX + "." + baseCommand.toLowerCase() + "." + subCommand.toLowerCase();
        Permission perm = new Permission(permStr);
        perm.addParent(ObservationDisplayer.PERM_PREFIX + "." + baseCommand + ".*", true);
        Bukkit.getPluginManager().addPermission(perm);
        this.permission = perm;
    }

    protected void description(String desc) { this.description = desc; }

    protected void arguments(String args) {
        String[] parsed = parseArgs(args, "[", "]");
        this.arguments.add(parsed);
        this.minArgs = 0;
        for (String arg : parsed) {
            if (arg.startsWith("[") && arg.endsWith("]")) {
                this.maxArgs += 2;
            } else {
                this.minArgs++;
                this.maxArgs++;
            }
        }
    }

    protected void bypassArgumentChecks() { this.bypassArgumentChecks = true; }

    protected void requiresPlayer() { this.requiresPlayer = true; }

    protected List<String> onTabComplete(CommandSender sender, String[] args) { return Arrays.asList(); }

    public List<String> executeOnTabComplete(CommandSender sender, String args[]) {
        if (!sender.hasPermission(getPermission()) || args.length > this.maxArgs) {
            return Arrays.asList();
        }
        return onTabComplete(sender, args);
    }

    private String formatArg(String arg) {
        List<String> options = Stream.of(arg.split(Pattern.quote("|")))
                .map(v -> ACCENT + v.replace("'", ACCENT + "\"" + SECONDARY))
                .collect(Collectors.toList());
        return PRIMARY + "<" + ACCENT + String.join(SEPARATOR + " | " + ACCENT, options) + PRIMARY + ">";
    }

    public String getCommand() {
        return PRIMARY + "/" + this.baseCommand + " " + SECONDARY + this.subCommand;
    }

    public List<String> getUsages() {
        List<String> res = new ArrayList<>();
        for (int ind = 0; ind < this.arguments.size(); ind++) {
            res.add(getUsage(ind));
        }
        return res;
    }

    public String getUsage(int index) {
        String usage = getCommand() + " ";
        for (String arg : this.arguments.get(index)) {
            usage += formatArg(arg) + " ";
        }
        return usage.trim();
    }

    public String getHelpLine() {
        return this.getCommand() + SEPARATOR + " - " + TEXT + this.description;
    }

    public Permission getPermission() {
        return this.permission;
    }

    protected abstract boolean onCommand(CommandSender sender, String[] args);

    public boolean executeSubCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            Utils.msg(sender,
                    "&cYou do not have the required permission!",
                    "  &f&o" + getPermission().getName());
            return true;
        }

        if (this.requiresPlayer && !(sender instanceof Player)) {
            Utils.msg(sender, ChatColor.RED + "You must be a player!");
            return true;
        }


        if (!this.bypassArgumentChecks && this.arguments.size() == 1) {
            if (args.length - 1 < this.minArgs) {
                List<String> missingArgsList = new ArrayList<>();
                String[] correctArgs = this.arguments.get(0);
                for (int ind = args.length - 1; ind < correctArgs.length; ind++) {
                    missingArgsList.add(formatArg(correctArgs[ind]));
                }
                String missingArgs = String.join("&7, ", missingArgsList);
                missingArguments(sender, missingArgs);
                return true;
            }
        }

        return onCommand(sender, parseArgs(Arrays.copyOfRange(args, 1, args.length), "\""));
    }

    protected void missingArguments(CommandSender sender, String missingArgs) {
        Utils.msg(sender, "&cMissing argument(s): " + missingArgs, "  " + getUsage(0));
    }

    private static String[] parseArgs(String[] args, String quote) {
        return parseArgs(String.join(" ", args), quote, quote);
    }

    protected static String[] parseArgs(String[] args, String start, String end) {
        return parseArgs(String.join(" ", args), start, end);
    }

    private static String[] parseArgs(String args, String start, String end) {
        String s = Pattern.quote(start);
        String e = Pattern.quote(end);
        Matcher matcher = Pattern.compile("([^" + s + "]\\S*|" + s + ".+?" + e + ")\\s*").matcher(args);

        List<String> res = new ArrayList<>();
        while (matcher.find())
            res.add(matcher.group(1).replace(start, "").replace(end, ""));

        return res.toArray(new String[0]);

    }

}
