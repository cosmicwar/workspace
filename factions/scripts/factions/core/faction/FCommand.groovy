package scripts.factions.core.faction

import com.google.common.base.Predicate
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.jetbrains.annotations.NotNull
import org.starcade.starlight.helper.Schedulers
import org.starcade.starlight.helper.command.AbstractCommand
import org.starcade.starlight.helper.command.CommandInterruptException
import org.starcade.starlight.helper.command.context.PlayerContext
import org.starcade.starlight.helper.command.functional.FunctionalCommandMessages
import org.starcade.starlight.helper.command.functional.FunctionalCommandPlayerHandler
import org.starcade.starlight.helper.utils.annotation.NonnullByDefault
import scripts.shared.legacy.utils.StringUtils

import java.util.function.Consumer

class FCBuilder {
    protected FCBuilder parentBuilder
    String[] baseCommands
    Consumer<Player> defaultAction = null

    List<FSC> subCommands = new ArrayList<>()

    TabbableFC tabbableFC = null

    FCBuilder(FCBuilder parentBuilder, String... baseCommands) {
        this.parentBuilder = parentBuilder
        this.baseCommands = baseCommands
    }

    FCBuilder(String... baseCommands) {
        this(null, baseCommands)
    }

    static FCBuilder of(String... baseCommands) {
        return new FCBuilder(baseCommands)
    }

    FSC create(String... subCommands) {
        return new FSC(subCommands, this)
    }

    FCBuilder defaultAction(Consumer<Player> defaultAction) {
        this.defaultAction = defaultAction
        return this
    }

    void build() {
        Schedulers.sync().execute {
            if (tabbableFC != null) {
                tabbableFC.close()
            }

            tabbableFC = new TabbableFC((PlayerContext handler) -> {
                if (handler.args().size() == 0) {
                    if (this.defaultAction == null) {
                        Bukkit.dispatchCommand(handler.sender(), "${handler.label()} help")
                    } else {
                        this.defaultAction.accept(handler.sender() as Player)
                    }
                    return
                }
                if (!handle(handler, this.subCommands)) {
                    handler.reply("§sList of sub-commands for §p${baseCommands[0]}§s:")
                    for (FSC subCommand : this.subCommands) {
                        handler.reply("  /${baseCommands[0]} §p${subCommand.aliases[0]} ${subCommand.usage == null ? "" : (subCommand.usage + " ")} §f- §s${subCommand.description ?: "No description."}")
                    }
                }
            }, subCommands).register(this.baseCommands)
        }
    }

    private static boolean handle(PlayerContext command, List<FSC> subCommands) {
        List<String> args = command.args()
        int size = args.size()

        if (size == 0) {
            return false
        }
        String executedSubCommand = args.get(0).toLowerCase()

        for (FSC subCommand : subCommands) {
            for (String alias : subCommand.getAliases()) {
                if (alias == executedSubCommand) {
                    if (subCommand.getSubCommands().isEmpty()) {
                        handleCommand(command, subCommand, size - 1, subCommand.getArgumentsNeded(), alias)
                        return true
                    } else {
                        if (size - 1 == 0) {
                            showUsage(command, subCommand)
                            return true
                        }
                        return handle(getCommand(command, alias), subCommand.getSubCommands())
                    }
                }
            }
        }
        return false
    }

    private static void handleCommand(PlayerContext command, FSC subCommand, int args, int neededArgs, String alias) {
        for (Predicate<PlayerContext> requirement : subCommand.getRequirements()) {
            if (!requirement.apply(command)) {
                return
            }
        }
        if (subCommand.permission != null && !command.sender().hasPermission(subCommand.permission)) {
            command.reply("§! §> §fYou do not have permission to execute this command!")
            return
        }
        if (args < neededArgs) {
            showUsage(command, subCommand)
        } else {
            subCommand.getHandler().handle(getCommand(command, alias))
        }
    }

    static String getChildUsage(FSC subCommand) {
        List<String> nodes = new ArrayList<>()

        nodes.add(subCommand.baseCommands[0])

        if (subCommand.getUsage() != null) {
            nodes.add(subCommand.getUsage())
        }
        List<FSC> subs = subCommand.getSubCommands()

        if (!subs.isEmpty()) {
            List<String> subBases = new ArrayList<>(subs.size())

            for (FSC sub : subs) {
                subBases.add(getChildUsage(sub))
            }
            nodes.add("(${StringUtils.asString(subBases, "/")})")
        }
        return StringUtils.asString(nodes)
    }

    static String getUsage(FSC subCommand, PlayerContext command = null) {
        List<String> nodes = new ArrayList<>()

        FCBuilder parent = subCommand

        while ((parent = parent.parent()) != null) {
            nodes.add(0, parent.baseCommands[0])
        }
        nodes.add(getChildUsage(subCommand))

        return "/${StringUtils.asString(nodes)}"
    }

    static void showUsage(PlayerContext command, FSC subCommand) {
        command.reply("&c${getUsage(subCommand, command)}")
    }

    private static <T extends CommandSender> PlayerContext getCommand(PlayerContext command, String alias) {
        return new PlayerContext(command.sender(), alias, Arrays.copyOfRange(command.args().toArray() as String[], 1, command.args().size()))
    }

    List<FSC> getSubCommands() {
        return this.subCommands
    }

    FCBuilder parent() {
        return this.parentBuilder
    }
}

class TabbableFC extends AbstractCommand implements TabCompleter {

    private final FunctionalCommandPlayerHandler handler
    private final List<FSC> subCommands

    TabbableFC(FunctionalCommandPlayerHandler handler, List<FSC> subCommands) {
        this.handler = handler
        this.subCommands = subCommands
    }

    def call(PlayerContext context) throws CommandInterruptException {
        this.handler.handle(context)
    }

    @Override
    boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            PlayerContext context = new PlayerContext((Player) sender, label, args);
            try {
                call(context);
            } catch (CommandInterruptException e) {
                e.getAction().accept(context.sender());
            }
        } else {
            sender.sendMessage(FunctionalCommandMessages.DEFAULT_NOT_PLAYER_MESSAGE);
        }

        return true;
    }

    @Override
    List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (subCommands.isEmpty()) return null

        List<FSC> subCommands = this.subCommands
        for (int i = 1; i < args.length; i++) {
            if (subCommands.isEmpty()) {
                break
            }

            FSC subCommand = subCommands.find { it.aliases.findAll { alias -> alias.equalsIgnoreCase(args[i - 1]) } }
            if (!subCommand) {
                break
            }

            if (subCommand.tabCompleter) {
                return subCommand.tabCompleter.handle(new PlayerContext(commandSender as Player, label, args))
            }


            subCommands = subCommand.getSubCommands().findAll {it.permission == null || commandSender.hasPermission(it.permission)}
        }

        return subCommands.isEmpty() ? null : subCommands.collect { it.aliases }.flatten() as List<String>
    }
}

class FSC extends FCBuilder
{
    private FunctionalCommandPlayerHandler handler
    private FunctionalCommandTabCompleter tabCompleter
    private String[] aliases
    private int argumentsNeeded = 0
    private String usage = null
    private String description = null
    private List<Predicate<PlayerContext>> requirements = new ArrayList<>()
    private String permission = null

    FSC(String[] aliases, FCBuilder builder) {
        super(builder, aliases)
        this.aliases = aliases
    }

    FCBuilder register(FunctionalCommandPlayerHandler handler) {
        this.handler = handler
        this.parentBuilder.subCommands.add(this)
        return this.parentBuilder
    }

    FSC registerSub(FunctionalCommandPlayerHandler handler) {
        return this.register(handler) as FSC
    }

    FSC args(int arguments) {
        this.argumentsNeeded = arguments
        return this
    }

    FSC usage(String usage) {
        this.usage = usage
        this.argumentsNeeded = usage.split(" ").findAll { it.startsWith("<") && it.endsWith(">") }.size()
        return this as FSC
    }

    FSC description(String description) {
        this.description = description
        return this
    }

    FSC require(Predicate<PlayerContext>... requirements) {
        this.requirements.addAll(requirements)
        return this
    }

    FSC requirePermission(String permission) {
        this.permission = permission
        return this
    }

    FSC tabCompletions(FunctionalCommandTabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter
        return this
    }

    FunctionalCommandPlayerHandler getHandler() {
        return this.handler
    }

    String[] getAliases() {
        return this.aliases
    }

    int getArgumentsNeded() {
        return this.argumentsNeeded
    }

    List<Predicate<PlayerContext>> getRequirements() {
        return this.requirements
    }

    String getUsage() {
        return this.usage
    }

    String getDescription() {
        return this.description
    }

    String getPermission() {
        return this.permission
    }

    FCBuilder parent() {
        return this.parentBuilder
    }

    FunctionalCommandTabCompleter getTabCompleter() {
        return this.tabCompleter
    }

}

@FunctionalInterface
@NonnullByDefault
interface FunctionalCommandTabCompleter {
    List<String> handle(PlayerContext context)
}
