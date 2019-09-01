package io.github.imsejin.console.serivce;

import java.io.IOException;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi.Attribute;
import com.diogonunes.jcdp.color.api.Ansi.BColor;
import com.diogonunes.jcdp.color.api.Ansi.FColor;

import io.github.imsejin.console.model.WorkingProcess;

/**
 * ConsoleService
 * 
 * @author SEJIN
 */
public class ConsoleService implements Runnable {

	private static final int PERCENT_MULTIPLES = 100;
	private static final int PROGRESS_BAR_LENGTH = 50;
	private final int compensatoryMultiples = PERCENT_MULTIPLES / PROGRESS_BAR_LENGTH;

	private WorkingProcess workingProcess;

	private static final ProcessBuilder COMMAND = new ProcessBuilder("cmd", "/c", "cls").inheritIO();
	private static final ColoredPrinter cp = new ColoredPrinter.Builder(1, false).foreground(FColor.WHITE)
			.background(BColor.BLACK).build();

	public void setWorkingProcess(WorkingProcess workingProcess) {
		this.workingProcess = workingProcess;
	}

	public synchronized void printProcessing() throws InterruptedException {
		String message = workingProcess.getMessage();
		int totalProcess = workingProcess.getTotalProcess();
		int i;

		// Gets changing current process and prints progress states
		while ((i = workingProcess.getCurrentProcess() + 1) < totalProcess) {
			// Calculates the percentage of current progress
			double percentage = ((double) i / totalProcess) * PERCENT_MULTIPLES;

			// Prints progress bar
			System.out.print(" |");
			for (int j = 0; j < PROGRESS_BAR_LENGTH; j++) {
				if (percentage > j * compensatoryMultiples) {
					cp.print(" ", Attribute.HIDDEN, FColor.GREEN, BColor.GREEN);
				} else {
					cp.print(" ", Attribute.HIDDEN, FColor.WHITE, BColor.WHITE);
				}
			}
			cp.clear();
			System.out.print("| ");

			// Prints currently processing item and current process
			System.out.print(message + "... ");
			System.out.print("(" + i + "/" + totalProcess + ")");

			// Enable console typewriter to overwrite them
			System.out.print("\r");
		}
	}

	public static void clear() {
		try {
			COMMAND.start().waitFor();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// Separates upper console logs
		System.out.println();

		try {
			printProcessing();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Seperates other progress or finish sentence
		System.out.println();
	}

}