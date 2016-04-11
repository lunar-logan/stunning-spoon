import csv
import re

stopset = set(['all', 'just', 'being', 'over', 'both', 'through', 'yourselves', 'its', 'before', 'herself', 'had', 'should', 'to', 'only', 'under', 'ours', 'has', 'do', 'them', 'his', 'very', 'they', 'not', 'during', 'now', 'him', 'nor', 'did', 'this', 'she', 'each', 'further', 'where', 'few', 'because', 'doing', 'some', 'are', 'our', 'ourselves', 'out', 'what', 'for', 'while', 'does', 'above', 'between', 't', 'be', 'we', 'who', 'were', 'here', 'hers', 'by', 'on', 'about', 'of', 'against', 's', 'or', 'own', 'into', 'yourself', 'down', 'your', 'from', 'her', 'their', 'there', 'been', 'whom', 'too', 'themselves', 'was', 'until', 'more', 'himself', 'that', 'but', 'don', 'with', 'than', 'those', 'he', 'me', 'myself', 'these', 'up', 'will', 'below', 'can', 'theirs', 'my', 'and', 'then', 'is', 'am', 'it', 'an', 'as', 'itself', 'at', 'have', 'in', 'any', 'if', 'again', 'no', 'when', 'same', 'how', 'other', 'which', 'you', 'after', 'most', 'such', 'why', 'a', 'off', 'i', 'yours', 'so', 'the', 'having', 'once'])


def _is_camel_case(s):
	return re.match(r'[a-z]*([A-Z]+[a-z]+)+', s)


def _break_camel_case(s):
	tokens, last_word = [], []
	for c in s:
		if c.isupper():
			if len(last_word) > 0:
				tokens.append(''.join(last_word))
			last_word = [c.lower()]
		else:
			last_word.append(c)
	if len(last_word) > 0:
		tokens.append(''.join(last_word))
	return tokens


def flatten(ls):
	if isinstance(ls, (list, tuple)):
		master = []
		for l in ls:
			master += flatten(l)
		return master
	else:
		return [ls]


def split_to_words(s):
	if _is_camel_case(s):
		return _break_camel_case(s)
	tokens = flatten(map(lambda w: _break_camel_case(w) if _is_camel_case(w) else w, re.split(r'[ _]+', s)))
	return tokens


def load_csv(csv):
	data = []
	for row in csv:
		data.append(map(lambda x: x.lower(), row))
	return data


def _approx_match(p, q):
	""" p, q are assumed to be string """
	assert type(p) is str and type(q) is str
	pset = set(split_to_words(p)) - stopset
	qset = set(split_to_words(q)) - stopset
	return pset.intersection(qset)


def are_similar(a, b):
	if len(a) != len(b):
		print "Error: length of both tuples are not same. Ignoring"
		return None

	sset = _approx_match(a[0], b[0])
	pset = _approx_match(a[1], b[1])
	oset = _approx_match(a[2], b[2])

	if len(sset) > 0 and len(oset) > 0:
		print "Match found for: \"{}\" \t \"{}\"".format(a, b)
		return True
	return False


def match(art, nat):
	matching, discarded = 0, 0

	for r in art:
		for rr in nat:
			t = are_similar(r, rr)
			if t is not None:
				if t:
					matching += 1
					break
			else:
				discarded += 1

	print "{} matching triplets out of {} generated and {} discarded".format(matching, len(art), discarded) 


def _test_accuracy(test_file_path, input_file_path):
	with open(test_file_path, 'rb') as testfd, open(input_file_path, 'rb') as inputfd:
		test_csv = csv.reader(testfd, delimiter='\t')
		input_csv = csv.reader(inputfd, delimiter='\t')

		natural = load_csv(test_csv)
		artificial = load_csv(input_csv)

		match(artificial, natural)


def main():
	test = "E:\\Coding\\Java Related\\Projects\\Sage\\data\\tomato_tsv.txt"
	inp = "E:\\Coding\\Java Related\\Projects\\Sage\\data\\Tomato.tsv"
	_test_accuracy(test, inp)


main()



