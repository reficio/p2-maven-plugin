package org.reficio.p2.resolver.impl.facade

import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br/>
 *         Reficio (TM) - Reestablish your software!<br/>
 *         http://www.reficio.org
 * @since 1.1.0
 */
abstract class AbstractAetherFacadeTest {

    abstract AetherFacade facade();

    abstract String expectedPackage();

    void assertCorrectType(object) {
        assertEquals(expectedPackage(),
                object.getClass().getPackage().getName().substring(0, expectedPackage().length()));
    }

    @Test
    void newDependencyRequest() {
        assertCorrectType(facade().newDependencyRequest(null, null))
    }

    @Test
    void newPreorderNodeListGenerator() {
        assertCorrectType(facade().newPreorderNodeListGenerator())
    }

    @Test
    void newCollectRequest() {
        assertCorrectType(facade().newCollectRequest())
    }

    @Test
    void newDependency() {
        assertCorrectType(facade().newDependency(facade().newDefaultArtifact("org.reficio:p2:1.0.0"), null))
    }

    @Test
    void newDefaultArtifact() {
        assertCorrectType(facade().newDefaultArtifact("org.reficio:p2:1.0.0"))
    }

    @Test
    void newArtifactRequest() {
        assertCorrectType(facade().newArtifactRequest())
    }

    @Test
    void newSubArtifact() {
        assertCorrectType(facade().newSubArtifact(facade().newDefaultArtifact("org.reficio:p2:1.0.0"), null, null))
    }

    @Test
    void newPatternExclusionsDependencyFilter() {
        assertCorrectType(facade().newPatternExclusionsDependencyFilter([]))
    }

}
