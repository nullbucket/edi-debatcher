# edi-debatcher

Original explanation of algorithim, prior to initial coding:
https://github.com/nullbucket/edi-debatcher/blob/master/edi-debatcher-algorithm.docx

If you base anything off of this, please give credit to the original author (me). Thanks.

The algorithm is sequential and constant in memory no matter how large the file. It does not use file or database tricks to do this. The code you see here is a very close (99%) match of the original algorithm. It was used in a live system that processed lots of EDI files in volume. Over time, it was enhanced by other developers to include metrics and logging; however, that version had tight coupling to those components, so this version is a refactor with most of the tight coupling removed.
