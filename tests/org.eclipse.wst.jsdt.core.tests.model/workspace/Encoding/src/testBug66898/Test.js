package testUTF8;
/**
 * <B>Farsi / Persian</B>: .???? ???? ?????????? ???????? ?????????? ?????? ???????? ?????????? 
 * <B>Russian:</B> ?? ?????????? ?????? ??????-?????? ??????????? ????, ???? ?????????????????? ??????????????! 
 * <B>Hungarian:</B> rv??zt??r?? t??k??rf??r??g??p. 
 * <B>Spanish:</B> El ping??ino Wenceslao hizo kil??metros bajo exhaustiva 
 *  lluvia y fr??o, a??oraba a su querido cachorro. 
 * <B>French:</B> Les na??fs ??githales h??tifs pondant ?? No??l o?? il g??le sont 
 *  s??rs d'??tre d????us et de voir leurs dr??les d'??ufs ab??m??s. </B>
 * <B>Esperanto:</B> E??oano ??iu??a??de. 
 */
public class Test {
	public static void main(String[] args) {
		System.out.println("Some sentences using UTF-8 encoded characters:");
		System.out.println("Farsi / Persian</B>: .???? ???? ?????????? ???????? ?????????? ?????? ???????? ??????????");
		System.out.println("Russian:</B> ?? ?????????? ?????? ??????-?????? ??????????? ????, ???? ?????????????????? ??????????????!");
		System.out.println("Hungarian:</B> rv??zt??r?? t??k??rf??r??g??p.");
		System.out.println("Spanish:</B> El ping??ino Wenceslao hizo kil??metros bajo exhaustiva lluvia y fr??o, a??oraba a su querido cachorro.");
		System.out.println("French:</B> Les na??fs ??githales h??tifs pondant ?? No??l o?? il g??le sont  s??rs d'??tre d????us et de voir leurs dr??les d'??ufs ab??m??s.");
		System.out.println("Esperanto:</B> E??oano ??iu??a??de. ");
	}
}