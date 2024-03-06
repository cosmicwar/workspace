package scripts.exec

import net.minecraft.world.level.block.entity.SignBlockEntity
import org.starcade.starlight.Starlight
import org.starcade.starlight.enviorment.Exports
import io.papermc.paper.configuration.GlobalConfiguration
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.IChatBaseComponent
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.SignText
import net.minecraft.world.level.block.entity.TileEntitySign
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.spigotmc.AsyncCatcher
import scripts.shared.legacy.utils.PacketUtils
import scripts.shared.legacy.utils.StringUtils
import scripts.shared.utils.preprocessors.VersionProcessor

AsyncCatcher.enabled = Globals.isDev
VersionProcessor.init()

Exports.ptr("pingOverride", { Player p ->
    //noinspection GrUnresolvedAccess
    return p.getHandle().e
})

Exports.ptr("signOpenOverride", { Player p, List<String> text ->
    Location location = p.getEyeLocation()

    location = location.clone().add(location.getDirection() * -3)

    p.sendBlockChange(new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ()), Material.OAK_WALL_SIGN.createBlockData())

    BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())


    CompoundTag tag = new CompoundTag()

    TileEntitySign sign = new TileEntitySign(pos, null);
    SignText signText = sign.a(true) // flag = front/back of sign
    for (int i = 0; i < text.size(); i++)
        signText = signText.a(i, IChatBaseComponent.a(text[i]));
    sign.a(signText, true);


    BlockEntity blockEntity = new BlockEntity(BlockEntityType.SIGN, pos, Blocks.OAK_SIGN.defaultBlockState()) {
        @Override
        CompoundTag getUpdateTag() {
            return tag
        }
    }

    for (int line = 0; line < 4; ++line) {
        tag.putString("Text${line + 1}", String.format("{\"text\":\"${StringUtils.color(text.get(line))}\"}"))
    }
    tag.putInt("x", pos.getX())
    tag.putInt("y", pos.getY())
    tag.putInt("z", pos.getZ())
    tag.putString("id", "minecraft:sign")

    ClientboundBlockEntityDataPacket entData = ClientboundBlockEntityDataPacket.create(blockEntity)
    ClientboundOpenSignEditorPacket editor = new ClientboundOpenSignEditorPacket(pos, true)

    PacketUtils.send(p, entData)
    PacketUtils.send(p, editor)
})

GlobalConfiguration.get().unsupportedSettings.performUsernameValidation = false

Starlight.watch(
        "scripts/shared/legacy/utils/FastItemUtils_impl.groovy",
        "scripts/shared/utils/PAPI.groovy",
        "~/WorldMaterial.groovy",
)
