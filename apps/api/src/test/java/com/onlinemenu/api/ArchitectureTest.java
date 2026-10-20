package com.onlinemenu.api;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Guards the architecture: layers inside each module and boundaries between modules.
 * Fails the build if someone, for example, calls a repository from a controller
 * or uses another module's entity directly.
 */
@AnalyzeClasses(packages = "com.onlinemenu.api", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String ROOT = "com.onlinemenu.api";

    // ---------- layers inside a module ----------

    @ArchTest
    static final ArchRule controllersDoNotUseRepositories = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule controllersDoNotUseEntities = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..entity..");

    @ArchTest
    static final ArchRule repositoriesAreUsedOnlyByServices = classes()
            .that().resideInAPackage("..repository..")
            .should().onlyBeAccessed().byAnyPackage("..service..", "..repository..");

    @ArchTest
    static final ArchRule servicesDoNotDependOnControllers = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..controller..");

    /** Everyone depends on interfaces (BranchService, StorageService ...), never on their implementations. */
    @ArchTest
    static final ArchRule implementationsAreNotUsedDirectly = noClasses()
            .that().resideOutsideOfPackage("..impl..")
            .should().dependOnClassesThat().resideInAPackage("..impl..");

    // ---------- boundaries between modules ----------

    /** Other modules may use only the public API of "branch": service, DTOs, events. */
    @ArchTest
    static final ArchRule branchInternalsArePrivate = noClasses()
            .that().resideOutsideOfPackage(ROOT + ".branch..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    ROOT + ".branch.entity..", ROOT + ".branch.repository..", ROOT + ".branch.mapper..");

    @ArchTest
    static final ArchRule menuInternalsArePrivate = noClasses()
            .that().resideOutsideOfPackage(ROOT + ".menu..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    ROOT + ".menu.entity..", ROOT + ".menu.repository..", ROOT + ".menu.mapper..");

    /** Other modules work with images only through ImageService, never with the storage directly. */
    @ArchTest
    static final ArchRule mediaInternalsArePrivate = noClasses()
            .that().resideOutsideOfPackage(ROOT + ".media..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    ROOT + ".media.storage..", ROOT + ".media.config..");

    /** Dependencies go one way: menu -> branch. Branch reacts to nothing in menu. */
    @ArchTest
    static final ArchRule branchDoesNotDependOnMenu = noClasses()
            .that().resideInAPackage(ROOT + ".branch..")
            .should().dependOnClassesThat().resideInAPackage(ROOT + ".menu..");

    @ArchTest
    static final ArchRule mediaDoesNotDependOnOtherModules = noClasses()
            .that().resideInAPackage(ROOT + ".media..")
            .should().dependOnClassesThat().resideInAnyPackage(ROOT + ".branch..", ROOT + ".menu..");
}
