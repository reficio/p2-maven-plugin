package org.reficio.p2.resolver.impl.facade

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br/>
 *         Reficio (TM) - Reestablish your software!<br/>
 *         http://www.reficio.org
 * @since 1.1.0
 */
class AetherSonatypeFacadeTest extends AbstractAetherFacadeTest {
    @Override
    AetherFacade facade() {
        return new AetherSonatypeFacade()
    }

    @Override
    String expectedPackage() {
        return "org.sonatype.aether."
    }
}
