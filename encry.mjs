import { addClassPath, loadFile } from 'nbb';

addClassPath("src")

const { handler } = await loadFile('./src/lambda/encry.cljs');

export { handler }