package edu.whimc.observationdisplayer.commands.observations;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import edu.whimc.observationdisplayer.Observation;
import edu.whimc.observationdisplayer.ObservationDisplayer;
import edu.whimc.observationdisplayer.commands.AbstractSubCommand;
import edu.whimc.observationdisplayer.utils.Utils;

public class ObservationsNear extends AbstractSubCommand {

    public ObservationsNear(ObservationDisplayer plugin, String baseCommand, String subCommand) {
        super(plugin, baseCommand, subCommand);
        super.description("Lists observations near you");
        super.arguments("radius");
        super.requiresPlayer();
    }

    @Override
    protected boolean onCommand(CommandSender sender, String[] args) {
        int radius;
        try {
            radius = Integer.parseInt(args[0]);
        } catch (NumberFormatException exc) {
            Utils.msg(sender, "&c\"&4" + args[0] + "&c\" is an invalid number!");
            return true;
        }

        double radiusSquared = Math.pow(radius, 2);
        Player player = (Player) sender;
        List<Observation> inRadius = Observation.getObservations().stream()
                .filter(v -> v.getViewLocation().getWorld() == player.getWorld())
                .filter(v -> v.getViewLocation().distanceSquared(player.getLocation()) <= radiusSquared)
                .collect(Collectors.toList());

        if (inRadius.isEmpty()) {
            Utils.msg(sender, "&cThere are no observations within &4" +
                    radius + " &cblock" + (radius == 1 ? "" : "s") + " of you!");
            return true;
        }

        Utils.msgNoPrefix(sender, "&7&m-----------------&r &9&lObservation List&r &7&m------------------",
                "  &9Radius: &7&o" + radius + " &7block" + (radius == 1 ? "" : "s"),
                "");
        inRadius.stream().forEachOrdered(v -> Utils.msgNoPrefix(sender, " &7- " + v.toString()));
        Utils.msgNoPrefix(sender, "&7&m-----------------------------------------------------");

        return true;
    }

}
