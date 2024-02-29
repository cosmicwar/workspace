package scripts.factions.content.entity.animation

import org.bukkit.util.EulerAngle

class Frame {
    private int frameID
    private float x, y, z, r
    private EulerAngle middle
    private EulerAngle rightLeg
    private EulerAngle leftLeg
    private EulerAngle rightArm
    private EulerAngle leftArm
    private EulerAngle head

    Frame mult(float a, int frameID) {
        Frame f = new Frame()
        f.frameID = frameID
        f.x = f.x * a
        f.y = f.y * a
        f.z = f.z * a
        f.r = f.r * a
        f.middle = new EulerAngle(middle.getX() * a, middle.getY() * a, middle.getZ() * a)
        f.rightLeg = new EulerAngle(rightLeg.getX() * a, rightLeg.getY() * a, rightLeg.getZ() * a)
        f.leftLeg = new EulerAngle(leftLeg.getX() * a, leftLeg.getY() * a, leftLeg.getZ() * a)
        f.rightArm = new EulerAngle(rightArm.getX() * a, rightArm.getY() * a, rightArm.getZ() * a)
        f.leftArm = new EulerAngle(leftArm.getX() * a, leftArm.getY() * a, leftArm.getZ() * a)
        f.head = new EulerAngle(head.getX() * a, head.getY() * a, head.getZ() * a)
        return f
    }

    Frame add(Frame a, int frameID) {
        Frame f = new Frame()
        f.frameID = frameID
        f.x = f.x + a.x
        f.y = f.y + a.y
        f.z = f.z + a.z
        f.r = f.r + a.r
        f.middle = new EulerAngle(middle.getX() + a.middle.getX(), middle.getY() + a.middle.getY(), middle.getZ() + a.middle.getZ())
        f.rightLeg = new EulerAngle(rightLeg.getX() + a.rightLeg.getX(), rightLeg.getY() + a.rightLeg.getY(), rightLeg.getZ() + a.rightLeg.getZ())
        f.leftLeg = new EulerAngle(leftLeg.getX() + a.leftLeg.getX(), leftLeg.getY() + a.leftLeg.getY(), leftLeg.getZ() + a.leftLeg.getZ())
        f.rightArm = new EulerAngle(rightArm.getX() + a.rightArm.getX(), rightArm.getY() + a.rightArm.getY(), rightArm.getZ() + a.rightArm.getZ())
        f.leftArm = new EulerAngle(leftArm.getX() + a.leftArm.getX(), leftArm.getY() + a.leftArm.getY(), leftArm.getZ() + a.leftArm.getZ())
        f.head = new EulerAngle(head.getX() + a.head.getX(), head.getY() + a.head.getY(), head.getZ() + a.head.getZ())
        return f
    }

    EulerAngle getHead() {
        return this.head
    }

    EulerAngle getLeftArm() {
        return this.leftArm
    }

    EulerAngle getLeftLeg() {
        return this.leftLeg
    }

    EulerAngle getMiddle() {
        return this.middle
    }

    EulerAngle getRightArm() {
        return this.rightArm
    }

    EulerAngle getRightLeg() {
        return this.rightLeg
    }

    float getR() {
        return this.r
    }

    float getX() {
        return this.x
    }

    float getY() {
        return this.y
    }

    float getZ() {
        return this.z
    }

    int getFrameID() {
        return this.frameID
    }

    void setHead(EulerAngle head) {
        this.head = head
    }

    void setLeftArm(EulerAngle leftArm) {
        this.leftArm = leftArm
    }

    void setLeftLeg(EulerAngle leftLeg) {
        this.leftLeg = leftLeg
    }

    void setMiddle(EulerAngle middle) {
        this.middle = middle
    }

    void setRightArm(EulerAngle rightArm) {
        this.rightArm = rightArm
    }

    void setRightLeg(EulerAngle rightLeg) {
        this.rightLeg = rightLeg
    }

    void setX(float x) {
        this.x = x
    }

    void setY(float y) {
        this.y = y
    }

    void setZ(float z) {
        this.z = z
    }

    void setR(float r) {
        this.r = r
    }

    void setFrameID(int frameID) {
        this.frameID = frameID
    }
}