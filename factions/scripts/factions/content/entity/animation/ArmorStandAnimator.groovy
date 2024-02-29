package scripts.factions.content.entity.animation

import org.bukkit.Location
import org.bukkit.entity.ArmorStand

class ArmorStandAnimator {
    public ArmorStand armorStand
    public int length
    public Frame[] frames
    public boolean paused = false
    public int currentFrame
    public Location startLocation
    public boolean interpolate

    ArmorStandAnimator(ArmorStandAnimatorTemplate template, ArmorStand armorStand) {
        this.armorStand = armorStand
        this.startLocation = armorStand.getLocation()

        this.frames = template.frames
        this.currentFrame = template.currentFrame
        this.interpolate = template.interpolate
        this.length = template.length
    }

    void pause() {
        this.paused = true
    }

    void stop() {
        this.currentFrame = 0
        this.update()
        this.currentFrame = 0
        this.paused = true
    }

    void play() {
        this.paused = false
    }

    void update() {
        if (!this.paused) {
            if (this.currentFrame >= (this.length - 1) || this.currentFrame < 0) {
                this.currentFrame = 0
            }
            Frame frame = this.frames[this.currentFrame]

            if (this.interpolate) {
                if (frame == null) {
                    frame = this.interpolate(this.currentFrame)
                }
            }
            if (frame != null) {
                Location location = this.startLocation.clone().add(frame.getX(), frame.getY(), frame.getZ())
                location.setYaw(frame.getR() + location.getYaw() as float)

                this.armorStand.teleport(location)
                this.armorStand.setBodyPose(frame.getMiddle())
                this.armorStand.setLeftLegPose(frame.getLeftLeg())
                this.armorStand.setRightLegPose(frame.getRightLeg())
                this.armorStand.setLeftArmPose(frame.getLeftArm())
                this.armorStand.setRightArmPose(frame.getRightArm())
                this.armorStand.setHeadPose(frame.getHead())
            }
            ++this.currentFrame
        }
    }

    private Frame interpolate(int frameID) {
        Frame minFrame = null

        for (int i = frameID; i >= 0; i--) {
            if (this.frames[i] != null) {
                minFrame = this.frames[i]
                break
            }
        }
        Frame maxFrame = null

        for (int i = frameID; i < this.frames.length; i++) {
            if (this.frames[i] != null) {
                maxFrame = this.frames[i]
                break
            }
        }
        Frame res = null

        if (maxFrame == null || minFrame == null) {
            if (maxFrame == null && minFrame != null) {
                return minFrame
            }
            if (minFrame == null && maxFrame != null) {
                return maxFrame
            }
            res = new Frame()
            res.setFrameID(frameID)
            return res
        }
        res = new Frame()
        res.setFrameID(frameID)

        float Dmin = frameID - minFrame.getFrameID()
        float D = maxFrame.getFrameID() - minFrame.getFrameID()
        float D0 = Dmin / D

        res = minFrame.mult(1 - D0 as float, frameID).add(maxFrame.mult(D0, frameID), frameID)

        return res
    }
}