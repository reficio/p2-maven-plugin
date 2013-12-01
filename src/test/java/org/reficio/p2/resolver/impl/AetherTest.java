package org.reficio.p2.resolver.impl;

import org.junit.Test;
import org.reficio.p2.resolver.impl.facade.AetherEclipseFacade;
import org.reficio.p2.resolver.impl.facade.AetherFacade;
import org.reficio.p2.resolver.impl.facade.AetherSonatypeFacade;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br/>
 *         Reficio (TM) - Reestablish your software!<br/>
 *         http://www.reficio.org
 * @since 1.1.0
 */
public class AetherTest {

    @Test
    public void facade_sonatypeAetherSystem() {
        // GIVEN
        Object repositorySystem = mock(org.sonatype.aether.RepositorySystem.class);

        // WHEN
        AetherFacade facade = Aether.facade(repositorySystem);

        // THEN
        assertTrue("Wrong facade type", facade instanceof AetherSonatypeFacade);
    }

    @Test
    public void facade_eclipseAetherSystem() {
        // GIVEN
        Object repositorySystem = mock(org.eclipse.aether.RepositorySystem.class);

        // WHEN
        AetherFacade facade = Aether.facade(repositorySystem);

        // THEN
        assertTrue("Wrong facade type", facade instanceof AetherEclipseFacade);
    }

}
