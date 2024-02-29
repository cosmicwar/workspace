package scripts.factions.content.entity.animation

import org.bukkit.util.EulerAngle

class ArmorStandAnimatorTemplate {
    public int length
    public Frame[] frames
    public int currentFrame = 0
    public boolean interpolate = false

    ArmorStandAnimatorTemplate(File file) {
        BufferedReader reader = new BufferedReader(new FileReader(file))

        String line

        Frame currentFrame = null

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("length")) {
                this.length = (int) Float.parseFloat(line.split(" ")[1])
                this.frames = new Frame[this.length]
            } else if (line.startsWith("frame")) {
                if (currentFrame != null) {
                    frames[currentFrame.getFrameID()] = currentFrame
                }
                int frameID = Integer.parseInt(line.split(" ")[1])
                currentFrame = new Frame()
                currentFrame.setFrameID(frameID)
            } else if (line.contains("interpolate")) {
                this.interpolate = true
            } else if (line.contains("Armorstand_Position")) {
                currentFrame.setX(Float.parseFloat(line.split(" ")[1]))
                currentFrame.setY(Float.parseFloat(line.split(" ")[2]))
                currentFrame.setZ(Float.parseFloat(line.split(" ")[3]))
                currentFrame.setR(Float.parseFloat(line.split(" ")[4]))
            } else if (line.contains("Armorstand_Middle")) {
                float x = (float) Math.toRadians(Float.parseFloat(line.split(" ")[1]))
                float y = (float) Math.toRadians(Float.parseFloat(line.split(" ")[2]))
                float z = (float) Math.toRadians(Float.parseFloat(line.split(" ")[3]))
                currentFrame.setMiddle(new EulerAngle(x, y, z))
            } else if (line.contains("Armorstand_Right_Leg")) {
                float x = (float) Math.toRadians(Float.parseFloat(line.split(" ")[1]))
                float y = (float) Math.toRadians(Float.parseFloat(line.split(" ")[2]))
                float z = (float) Math.toRadians(Float.parseFloat(line.split(" ")[3]))
                currentFrame.setRightLeg(new EulerAngle(x, y, z))
            } else if (line.contains("Armorstand_Left_Leg")) {
                float x = (float) Math.toRadians(Float.parseFloat(line.split(" ")[1]))
                float y = (float) Math.toRadians(Float.parseFloat(line.split(" ")[2]))
                float z = (float) Math.toRadians(Float.parseFloat(line.split(" ")[3]))
                currentFrame.setLeftLeg(new EulerAngle(x, y, z))
            } else if (line.contains("Armorstand_Left_Arm")) {
                float x = (float) Math.toRadians(Float.parseFloat(line.split(" ")[1]))
                float y = (float) Math.toRadians(Float.parseFloat(line.split(" ")[2]))
                float z = (float) Math.toRadians(Float.parseFloat(line.split(" ")[3]))
                currentFrame.setLeftArm(new EulerAngle(x, y, z))
            } else if (line.contains("Armorstand_Right_Arm")) {
                float x = (float) Math.toRadians(Float.parseFloat(line.split(" ")[1]))
                float y = (float) Math.toRadians(Float.parseFloat(line.split(" ")[2]))
                float z = (float) Math.toRadians(Float.parseFloat(line.split(" ")[3]))
                currentFrame.setRightArm(new EulerAngle(x, y, z))
            } else if (line.contains("Armorstand_Head")) {
                float x = (float) Math.toRadians(Float.parseFloat(line.split(" ")[1]))
                float y = (float) Math.toRadians(Float.parseFloat(line.split(" ")[2]))
                float z = (float) Math.toRadians(Float.parseFloat(line.split(" ")[3]))
                currentFrame.setHead(new EulerAngle(x, y, z))
            }
        }
        if (currentFrame != null) {
            this.frames[currentFrame.getFrameID()] = currentFrame
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