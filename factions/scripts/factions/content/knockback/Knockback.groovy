package scripts.factions.content.knockback

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.minecraft.util.Mth
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.starcade.starlight.helper.Commands

@CompileStatic(TypeCheckingMode.SKIP)
class Knockback
{

    Knockback()
    {
        Commands.create().assertPlayer().assertOp().handler {ctx ->
            def sender = (ctx.sender() as CraftPlayer).getHandle()
            def target = ctx.arg(0).parseOrFail(Player)

            def entity = (target as CraftPlayer).getHandle()
            entity.knockback(
                    1,
                    Mth.sin(sender.getYRot() * 0.017453292F as float),
                    -Mth.cos(sender.getYRot() * 0.017453292F as float),
                    sender
            ); // Paper

//            target.push(
//                    -Mth.sin(this.getYRot() * 0.017453292F) * (float) i * 0.5F,
//                    0.1D,
//                    Mth.cos(this.getYRot() * 0.017453292F) * (float) i * 0.5F,
//                    this
//            ); // Paper
        }.register("testkb")
    }

}
