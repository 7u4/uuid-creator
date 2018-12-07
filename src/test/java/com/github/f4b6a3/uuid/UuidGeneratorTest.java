package com.github.f4b6a3.uuid;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.github.f4b6a3.uuid.random.Xorshift128PlusRandom;
import com.github.f4b6a3.test.other.RaceConditionRunnable;
import com.github.f4b6a3.test.other.RandomImage;
import com.github.f4b6a3.test.other.RandomnesTest;
import com.github.f4b6a3.test.other.SimpleBenchmark;
import com.github.f4b6a3.uuid.UuidGenerator;
import com.github.f4b6a3.uuid.factory.MssqlUuidCreator;
import com.github.f4b6a3.uuid.factory.abst.AbstractUuidCreator;
import com.github.f4b6a3.uuid.random.Xoroshiro128PlusRandom;
import com.github.f4b6a3.uuid.random.XorshiftRandom;
import com.github.f4b6a3.uuid.random.XorshiftStarRandom;
import com.github.f4b6a3.uuid.util.ByteUtil;
import com.github.f4b6a3.uuid.util.TimestampUtil;
import com.github.f4b6a3.uuid.util.UuidUtil;

import static com.github.f4b6a3.uuid.util.ByteUtil.*;

/**
 * Unit test for uuid-generator.
 */
public class UuidGeneratorTest {

	private static final long DEFAULT_LOOP_LIMIT = 100;

	private static final String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-5][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

	@Test
	public void testGetRandomUuid_StringIsValid() {
		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {
			UUID uuid = UuidGenerator.getRandom();
			assertTrue(uuid.toString().matches(UuidGeneratorTest.UUID_PATTERN));
		}
	}

	@Test
	public void testGetTimeBasedUuid_StringIsValid() {
		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {
			UUID uuid = UuidGenerator.getTimeBased();
			assertTrue(uuid.toString().matches(UuidGeneratorTest.UUID_PATTERN));
		}
	}

	@Test
	public void testGetTimeBasedWithMac_StringIsValid() {
		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {
			UUID uuid = UuidGenerator.getTimeBasedWithMac();
			assertTrue(uuid.toString().matches(UuidGeneratorTest.UUID_PATTERN));
		}
	}

	@Test
	public void testGetOrderedUuid_StringIsValid() {
		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {
			UUID uuid = UuidGenerator.getOrdered();
			assertTrue(uuid.toString().matches(UuidGeneratorTest.UUID_PATTERN));
		}
	}

	@Test
	public void testGetOrderedWithMac_StringIsValid() {
		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {
			UUID uuid = UuidGenerator.getOrderedWithMac();
			assertTrue(uuid.toString().matches(UuidGeneratorTest.UUID_PATTERN));
		}
	}

	@Test
	public void testGetOrdered_TimestampBitsAreOrdered() {

		long oldTimestemp = 0;
		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {
			UUID uuid = UuidGenerator.getOrdered();
			long newTimestamp = UuidUtil.extractTimestamp(uuid);
			
			if(i > 0) {
				assertTrue(newTimestamp >= oldTimestemp);
			}
			oldTimestemp = newTimestamp;
		}
	}

	@Test
	public void testGetOrdered_MostSignificantBitsAreOrdered() {

		long oldMsb = 0;

		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {
			UUID uuid = UuidGenerator.getOrdered();
			long newMsb = uuid.getMostSignificantBits();

			if(i > 0) {
				assertTrue(newMsb >= oldMsb);
			}
			oldMsb = newMsb;
		}
	}

	/**
	 * Test if a time based UUID version 1 is has the correct timestamp.
	 */
	@Test
	public void testGetTimeBased_TimestampIsCorrect() {
		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {

			Instant instant1 = Instant.now();

			UUID uuid = UuidGenerator.getTimeBasedCreator().withInstant(instant1).create();
			Instant instant2 = UuidUtil.extractInstant(uuid);

			long timestamp1 = TimestampUtil.toTimestamp(instant1);
			long timestamp2 = TimestampUtil.toTimestamp(instant2);

			assertEquals(timestamp1, timestamp2);
		}
	}

	/**
	 * Test if a ordered UUID version 0 is has the correct timestamp.
	 */
	@Test
	public void testGetOrdered_TimestampIsCorrect() {
		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {

			Instant instant1 = Instant.now();

			UUID uuid = UuidGenerator.getOrderedCreator().withInstant(instant1).create();
			Instant instant2 = UuidUtil.extractInstant(uuid);

			long timestamp1 = TimestampUtil.toTimestamp(instant1);
			long timestamp2 = TimestampUtil.toTimestamp(instant2);

			assertEquals(timestamp1, timestamp2);
		}
	}

	/**
	 * Test if a DCE Security version 2 has correct local domain and identifier.
	 */
	@Test
	public void testGetDCESecurityl_DomainAndIdentifierAreCorrect() {
		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {

			byte localDomain = (byte) i;
			int localIdentifier = 1701;

			UUID uuid = UuidGenerator.getDceSecurity(localDomain, localIdentifier);

			byte localDomain2 = UuidUtil.extractDceSecurityLocalDomain(uuid);
			int localIdentifier2 = UuidUtil.extractDceSecurityLocalIdentifier(uuid);

			assertEquals(localDomain, localDomain2);
			assertEquals(localIdentifier, localIdentifier2);
		}
	}

	/**
	 * Test if a name-based UUID version 3 with name space is correct.
	 */
	@Test
	public void testGetNameBasedMd5() {

		UUID namespace = AbstractUuidCreator.NAMESPACE_DNS;
		String name = null;
		UUID uuid = null;

		for (int i = 0; i < DEFAULT_LOOP_LIMIT; i++) {

			name = UuidGenerator.getRandom().toString();
			uuid = UuidGenerator.getNameBasedMd5(namespace, name);

			byte[] namespaceBytes = toBytes(namespace.toString().replaceAll("-", ""));
			byte[] nameBytes = name.getBytes();
			byte[] bytes = concat(namespaceBytes, nameBytes);

			assertEquals(UUID.nameUUIDFromBytes(bytes).toString(), uuid.toString());
		}
	}

	@Test
	public void testGetNameBasedMd5_test_dns_www_github_com() {
		UUID namespace = AbstractUuidCreator.NAMESPACE_DNS;
		String name = "www.github.com";
		
		// Value generated by UUIDGEN (util-linux packege)
		UUID uuid1 = UUID.fromString("2c02fba1-0794-3c12-b62b-578ec5f03908");
		UUID uuid2 = UuidGenerator.getNameBasedMd5(namespace, name);
		assertEquals(uuid1, uuid2);
	}
	
	@Test
	public void testGetNameBasedSha1_test_dns_www_github_com() {

		UUID namespace = AbstractUuidCreator.NAMESPACE_DNS;
		String name = "www.github.com";
		
		// Value generated by UUIDGEN (util-linux packege)
		UUID uuid1 = UUID.fromString("04e16ed4-cd93-55f3-b2e3-1a097fc19832");
		UUID uuid2 = UuidGenerator.getNameBasedSha1(namespace, name);
		assertEquals(uuid1, uuid2);

	}
	
	/**
	 * Test with many threads running at the same time.
	 * 
	 * It basically tests if a UUID is generated more than once. If it occurs, a
	 * {@link RuntimeException} is thrown.
	 * 
	 * It's expected to see overrun warnings, since there's no exception thrown.
	 */
	@Ignore
	public void testRaceCondition() {
		for (int i = 0; i < RaceConditionRunnable.threadCount; i++) {
			Thread thread = new Thread(new RaceConditionRunnable(i));
			thread.start();
		}
	}

	/**
	 * This method only prints average running times.
	 */
	@Ignore
	public void testEstimateRunningTimes() {
		long loopMax = 100_000;
		long nano = 1_000_000;
		long randomUUID = (SimpleBenchmark.run(null, UUID.class, "randomUUID", loopMax) * loopMax) / nano;
		long getRandomUUID = (SimpleBenchmark.run(null, UuidGenerator.class, "getRandom", loopMax) * loopMax)
				/ nano;
		long getTimeBasedUUID = (SimpleBenchmark.run(null, UuidGenerator.class, "getTimeBased", loopMax) * loopMax)
				/ nano;
		long getOrderedUUID = (SimpleBenchmark.run(null, UuidGenerator.class, "getOrdered", loopMax)
				* loopMax) / nano;
		long javaNextLong = (SimpleBenchmark.run(new Random(), null, "nextLong", loopMax) * loopMax) / nano;
		long XorshiftNextLong = (SimpleBenchmark.run(new XorshiftRandom(), null, "nextLong", loopMax) * loopMax) / nano;

		System.out.println();
		System.out.println("----------------------------------------");
		System.out.println(String.format("Average generation times for %,d UUIDs", loopMax));
		System.out.println("----------------------------------------");
		System.out.println(String.format("* java.util.UUID.randomUUID():       %s ms", randomUUID));
		System.out.println(String.format("* UUIDGenerator.getRandomUUID():     %s ms", getRandomUUID));
		System.out.println(String.format("* UUIDGenerator.getTimeBasedUUID():  %s ms", getTimeBasedUUID));
		System.out.println(String.format("* UUIDGenerator.getOrderedUUID(): %s ms", getOrderedUUID));
		System.out.println(String.format("* java.util.Random.nextLong(): %s ms", javaNextLong));
		System.out.println(String.format("* XorshiftRandom.nextLong(): %s ms", XorshiftNextLong));
		System.out.println("----------------------------------------");
	}

	/**
	 * Just prints UUIDs generated to a specific instant.
	 */
	@Ignore
	public void testDemoDifferenceBetweenTimeBasedAndOrderedUUID() {

		Instant instant = Instant.now();
		String timeBasedUUID = UuidGenerator.getTimeBasedCreator().withInstant(instant).create().toString();
		String orderedUUID = UuidGenerator.getOrderedCreator().withInstant(instant).create().toString();

		System.out.println();
		System.out.println("----------------------------------------");
		System.out.println("Demonstration of time-baed UUIDs");
		System.out.println("----------------------------------------");
		System.out.println("- TimeBased UUID:          " + timeBasedUUID.toString());
		System.out.println("- Ordered UUID:         " + orderedUUID.toString());
		System.out.println("- Original instant:        " + instant.toString());
		System.out.println("- TimeBased UUID instant:  " + UuidUtil.extractInstant(UUID.fromString(timeBasedUUID)));
		System.out.println("- Ordered UUID instant: " + UuidUtil.extractInstant(UUID.fromString(orderedUUID)));
		System.out.println("----------------------------------------");
	}

	@Ignore
	public void testPrintList() {
		int max = 10000;

		System.out.println();
		System.out.println("----------------------------------------");
		System.out.println("Print list of UUIDs");
		System.out.println("----------------------------------------");

		System.out.println();
		System.out.println("### Random UUID");

		for (int i = 0; i < max; i++) {
			System.out.println(UuidGenerator.getFastRandom());
		}

		System.out.println();
		System.out.println("### Time-based UUID");

		for (int i = 0; i < max; i++) {
			System.out.println(UuidGenerator.getTimeBased());
		}

		System.out.println();
		System.out.println("### Ordered UUID");

		for (int i = 0; i < max; i++) {
			System.out.println(UuidGenerator.getOrdered());
		}

		System.out.println("----------------------------------------");
	}

	/**
	 * Generate images with random pixels.
	 */
	@Ignore
	public void testCreateRandomImage() {

		System.out.println();
		System.out.println("----------------------------------------");
		System.out.println("Creating images from random numbers...");
		System.out.println("----------------------------------------");

		RandomImage.createRandomImageFile("/tmp/java.util.Random.png", new Random(), 0, 0);
		RandomImage.createRandomImageFile("/tmp/java.security.SecureRandom.png", new SecureRandom(), 0, 0);
		RandomImage.createRandomImageFile("/tmp/XorshiftRandom.png", new XorshiftRandom(), 0, 0);
		RandomImage.createRandomImageFile("/tmp/XorshiftStarRandom.png", new XorshiftStarRandom(), 0, 0);
		RandomImage.createRandomImageFile("/tmp/Xorshift128PlusRandom.png", new Xorshift128PlusRandom(), 0, 0);
		RandomImage.createRandomImageFile("/tmp/Xoroshiro128PlusRandom.png", new Xoroshiro128PlusRandom(), 0, 0);

		System.out.println("----------------------------------------");
	}

	/**
	 * Test randomness of a random number generator.
	 * 
	 * If ENT is not installed, just annotate this test with @Ignore.
	 * 
	 * @param path
	 * @param random
	 */
	@Ignore
	public void testPseudoNumberSequence() throws Exception {

		Random random = new SecureRandom();
		// Random random = new Random();
		// Random random = new XorshiftRandom();
		// Random random = new XorshiftStarRandom();
		// Random random = new Xorshift128PlusRandom();
		// Random random = new Xoroshirot128PlusRandom();

		String path = "/tmp/testPseudoNumberSequence.dat";

		System.out.println();
		System.out.println("----------------------------------------");
		System.out.println("Test pseudo-number sequence");
		System.out.println("----------------------------------------");

		System.out.println(String.format("ent %s\n", path));
		RandomnesTest.runPseudoNumberSequenceTestProgram(path, random);

		System.out.println("----------------------------------------");
	}
}