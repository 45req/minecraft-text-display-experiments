fun cloak(
    world: World,
    position: Vector,
    rotation: Quaternionf,
    eyePosition: Vector,
    alpha: Int,
    noiseAmount: Double,
): RenderEntityGroup {

    // Force alpha to 0 (fully transparent)
    val alpha = 0

    val xSize = 1f
    val ySize = 1.5f

    val xItems = 16
    val yItems = (xItems * ySize / xSize).toInt()

    val forwardDistance = 1.5f
    val castDistance = 100.0

    val group = RenderEntityGroup()
    for (x in 0 until xItems) {
        for (y in 0 until yItems) {
            val xItemSize = xSize / xItems
            val yItemSize = ySize / yItems

            val transform = Matrix4f()
                .rotate(rotation)
                .translate(-xSize / 2f, -.5f - ySize / 2f, 0f)
                .translate(x * xItemSize, y * yItemSize, forwardDistance)
                .scale(xItemSize, yItemSize, 1f)

            group.add(x to y, textRenderEntity(
                world = world,
                position = position,
                init = {
                    it.text = " "
                    it.interpolationDuration = 1
                    it.teleportDuration = 1
                },
                update = {
                    it.interpolateTransform(Matrix4f(transform).mul(textBackgroundTransform))

                    val relative = transform.transform(Vector4f(.5f,.5f,.0f,1f)).toVector3f()
                    val center = position.clone().add(Vector().copy(relative))
                    val direction = center.subtract(eyePosition).normalize()

                    val hitBlock = world.raycastGround(eyePosition, direction, castDistance)
                    val data = hitBlock?.hitBlock?.blockData

                    val newColor = if (data !== null) {
                        val color = getColorFromBlock(data) ?: it.backgroundColor ?: return@textRenderEntity

                        val positionSeed = hitBlock.hitPosition
                        val seed = (positionSeed.x * 1000 + positionSeed.y * 100 + positionSeed.z * 10).toInt() + x * 10000 + y * 10_0000

                        color.noise(noiseAmount, seed).setAlpha(alpha) // Apply alpha to the block color
                    } else {
                        val skyBottomColor = Color.fromRGB(0xd6edfa)
                        val skyTopColor = Color.fromRGB(0x5e92d4)
                        val skyBottom = (-20.0).toRadians()
                        val skyTop = 40.0.toRadians()
                        val pitch = direction.pitch()

                        val fraction = ((pitch - skyBottom) / (skyTop - skyBottom)).coerceIn(.0, 1.0)

                        skyBottomColor.lerpOkLab(skyTopColor, fraction).setAlpha(alpha) // Apply alpha to the sky color
                    }

                    it.backgroundColor = (it.backgroundColor ?: newColor).lerpOkLab(newColor, .3)
                }
            ))

        }
    }

    return group
}
